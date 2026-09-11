package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.entity.OrderInfoDO;
import com.mall.entity.ProductDO;
import com.mall.entity.UserDO;
import com.mall.mapper.OrderInfoMapper;
import com.mall.mapper.ProductMapper;
import com.mall.mapper.UserMapper;
import com.mall.service.DashboardService;
import com.mall.vo.DashboardOverviewVO;
import com.mall.vo.OrderStatusStatVO;
import com.mall.vo.RecentOrderVO;
import com.mall.vo.SalesTrendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dashboard统计Service实现
 *
 * @author xiu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderInfoMapper orderInfoMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    /**
     * 获取概览统计数据
     */
    @Override
    public DashboardOverviewVO getOverview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // 昨日时间范围
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);

        // 今日订单数
        Long todayOrders = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfoDO>()
                .ge(OrderInfoDO::getCreateTime, todayStart)
                .lt(OrderInfoDO::getCreateTime, todayEnd))
                .longValue();

        // 今日销售额（只统计已支付和已完成的订单）
        BigDecimal todaySales = orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfoDO>()
                        .ge(OrderInfoDO::getCreateTime, todayStart)
                        .lt(OrderInfoDO::getCreateTime, todayEnd)
                        .in(OrderInfoDO::getOrderStatus, 2, 3, 4))
                .stream()
                .map(OrderInfoDO::getPayPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 今日新增用户
        Long newUsers = userMapper.selectCount(new LambdaQueryWrapper<UserDO>()
                        .ge(UserDO::getCreateTime, todayStart)
                        .lt(UserDO::getCreateTime, todayEnd))
                .longValue();

        // 库存预警商品数（库存 <= 10）
        Long lowStockProducts = productMapper.selectCount(new LambdaQueryWrapper<ProductDO>()
                        .le(ProductDO::getStock, 10)
                        .eq(ProductDO::getStatus, 1))
                .longValue();

        // 计算增长率
        // 昨日订单数
        Long yesterdayOrders = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfoDO>()
                        .ge(OrderInfoDO::getCreateTime, yesterdayStart)
                        .lt(OrderInfoDO::getCreateTime, yesterdayEnd))
                .longValue();

        // 昨日销售额
        BigDecimal yesterdaySales = orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfoDO>()
                        .ge(OrderInfoDO::getCreateTime, yesterdayStart)
                        .lt(OrderInfoDO::getCreateTime, yesterdayEnd)
                        .in(OrderInfoDO::getOrderStatus, 2, 3, 4))
                .stream()
                .map(OrderInfoDO::getPayPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 昨日新增用户
        Long yesterdayUsers = userMapper.selectCount(new LambdaQueryWrapper<UserDO>()
                        .ge(UserDO::getCreateTime, yesterdayStart)
                        .lt(UserDO::getCreateTime, yesterdayEnd))
                .longValue();

        vo.setTodayOrders(todayOrders);
        vo.setTodaySales(todaySales);
        vo.setNewUsers(newUsers);
        vo.setLowStockProducts(lowStockProducts);
        vo.setOrderGrowth(calculateGrowth(todayOrders, yesterdayOrders));
        vo.setSalesGrowth(calculateGrowth(todaySales, yesterdaySales));
        vo.setUserGrowth(calculateGrowth(newUsers, yesterdayUsers));

        return vo;
    }

    /**
     * 获取销售趋势
     */
    @Override
    public SalesTrendVO getSalesTrend(String period) {
        SalesTrendVO vo = new SalesTrendVO();
        List<SalesTrendVO.TrendData> trendData = new ArrayList<>();

        LocalDate today = LocalDate.now();
        int days;
        String dateFormat;

        switch (period) {
            case "today" -> {
                days = 1;
                dateFormat = "HH:00";
            }
            case "week" -> {
                days = 7;
                dateFormat = "MM-dd";
            }
            case "month" -> {
                days = 30;
                dateFormat = "MM-dd";
            }
            case "year" -> {
                days = 12;
                dateFormat = "yyyy-MM";
            }
            default -> {
                days = 7;
                dateFormat = "MM-dd";
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);

        // 按指定周期获取数据
        if ("year".equals(period)) {
            // 按月统计
            for (int i = 11; i >= 0; i--) {
                LocalDate month = today.minusMonths(i);
                LocalDateTime start = month.withDayOfMonth(1).atStartOfDay();
                LocalDateTime end = month.withDayOfMonth(month.lengthOfMonth()).atTime(LocalTime.MAX);

                SalesTrendVO.TrendData data = new SalesTrendVO.TrendData();
                data.setDate(month.format(DateTimeFormatter.ofPattern("yyyy-MM")));

                Integer orders = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfoDO>()
                        .ge(OrderInfoDO::getCreateTime, start)
                        .lt(OrderInfoDO::getCreateTime, end)
                        .in(OrderInfoDO::getOrderStatus, 2, 3, 4)).intValue();

                BigDecimal sales = orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfoDO>()
                                .ge(OrderInfoDO::getCreateTime, start)
                                .lt(OrderInfoDO::getCreateTime, end)
                                .in(OrderInfoDO::getOrderStatus, 2, 3, 4))
                        .stream()
                        .map(OrderInfoDO::getPayPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                data.setOrders(orders);
                data.setSales(sales);
                trendData.add(data);
            }
        } else {
            // 按天/小时统计
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.atTime(LocalTime.MAX);

                SalesTrendVO.TrendData data = new SalesTrendVO.TrendData();
                data.setDate(date.format(formatter));

                Integer orders = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfoDO>()
                        .ge(OrderInfoDO::getCreateTime, start)
                        .lt(OrderInfoDO::getCreateTime, end)
                        .in(OrderInfoDO::getOrderStatus, 2, 3, 4)).intValue();

                BigDecimal sales = orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfoDO>()
                                .ge(OrderInfoDO::getCreateTime, start)
                                .lt(OrderInfoDO::getCreateTime, end)
                                .in(OrderInfoDO::getOrderStatus, 2, 3, 4))
                        .stream()
                        .map(OrderInfoDO::getPayPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                data.setOrders(orders);
                data.setSales(sales);
                trendData.add(data);
            }
        }

        vo.setTrendData(trendData);
        return vo;
    }

    /**
     * 获取订单状态统计
     */
    @Override
    public OrderStatusStatVO getOrderStatusStat() {
        OrderStatusStatVO vo = new OrderStatusStatVO();

        Long total = orderInfoMapper.selectCount(null);

        Long pending = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfoDO>()
                .eq(OrderInfoDO::getOrderStatus, 1));
        Long processing = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfoDO>()
                .eq(OrderInfoDO::getOrderStatus, 2));
        Long shipped = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfoDO>()
                .eq(OrderInfoDO::getOrderStatus, 3));
        Long completed = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfoDO>()
                .eq(OrderInfoDO::getOrderStatus, 4));
        Long cancelled = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfoDO>()
                .eq(OrderInfoDO::getOrderStatus, 5));

        vo.setPending(pending.intValue());
        vo.setProcessing(processing.intValue());
        vo.setShipped(shipped.intValue());
        vo.setCompleted(completed.intValue());
        vo.setCancelled(cancelled.intValue());
        vo.setTotal(total.intValue());

        return vo;
    }

    /**
     * 获取最新订单
     */
    @Override
    public List<RecentOrderVO> getRecentOrders(Integer limit) {
        List<OrderInfoDO> orders = orderInfoMapper.selectList(
                new LambdaQueryWrapper<OrderInfoDO>()
                        .orderByDesc(OrderInfoDO::getCreateTime)
                        .last("LIMIT " + limit)
        );

        return orders.stream().map(order -> {
            RecentOrderVO vo = new RecentOrderVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setUserId(order.getUserId());
            vo.setPayPrice(order.getPayPrice());
            vo.setOrderStatus(order.getOrderStatus());
            vo.setCreateTime(order.getCreateTime());

            // 获取用户名
            if (order.getUserId() != null) {
                UserDO user = userMapper.selectById(order.getUserId());
                if (user != null) {
                    vo.setUserNickname(maskName(user.getNickname() != null ? user.getNickname() : user.getUsername()));
                }
            }

            // 状态名称
            vo.setOrderStatusName(getStatusName(order.getOrderStatus()));

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 计算增长率
     */
    private <T extends Number> BigDecimal calculateGrowth(T current, T previous) {
        if (previous.doubleValue() == 0) {
            return current.doubleValue() > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        double growth = (current.doubleValue() - previous.doubleValue()) / previous.doubleValue() * 100;
        return BigDecimal.valueOf(growth).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "待支付";
            case 2 -> "处理中";
            case 3 -> "已发货";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> "未知";
        };
    }

    /**
     * 脱敏用户名
     */
    private String maskName(String name) {
        if (name == null || name.length() < 2) {
            return "*";
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*" + name.charAt(name.length() - 1);
    }
}
