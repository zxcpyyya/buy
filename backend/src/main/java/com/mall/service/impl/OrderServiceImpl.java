package com.mall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.dto.CreateOrderDTO;
import com.mall.entity.*;
import com.mall.mapper.*;
import com.mall.service.OrderService;
import com.mall.service.ProductService;
import com.mall.service.CouponService;
import com.mall.service.PointsService;
import com.mall.service.ExpressService;
import com.mall.sharding.ShardingHintUtils;
import com.mall.sharding.ShardingKeyUtils;
import com.mall.vo.OrderItemVO;
import com.mall.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 订单Service实现类
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final UserMapper userMapper;
    private final ProductService productService;
    private final CouponService couponService;
    private final PointsService pointsService;
    private final ExpressService expressService;
    
    /**
     * 创建订单
     * 
     * 
     * 1. 使用@Transactional保证事务一致性
     * 2. 防止并发超卖问题
     * 3. 完善的异常处理
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderVO createOrder(CreateOrderDTO createOrderDTO, Long userId) {
        // 1. 校验收货地址（NPE防护）
        AddressDO address = addressMapper.selectById(createOrderDTO.getAddressId());
        if (Objects.isNull(address) || !address.getUserId().equals(userId)) {
            throw new BusinessException("A0401", "收货地址不存在或无权限");
        }
        
        // 2. 获取购物车商品（集合处理使用isEmpty()判断）
        List<CartItemDO> cartItems = cartItemMapper.selectList(
            new LambdaQueryWrapper<CartItemDO>()
                .eq(CartItemDO::getCartId, createOrderDTO.getCartId())
        );
        
        if (CollectionUtils.isEmpty(cartItems)) {
            throw new BusinessException("A0401", "购物车为空");
        }
        
        // 3. 计算订单金额（货币金额使用BigDecimal）
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItemDO item : cartItems) {
            BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(subtotal);
        }

        // 4. 计算优惠（积分和优惠券）
        BigDecimal couponDiscount = BigDecimal.ZERO;
        BigDecimal pointsDiscount = BigDecimal.ZERO;
        String couponName = null;

        // 4.1 处理优惠券
        if (createOrderDTO.getCouponId() != null) {
            couponDiscount = couponService.calculateDiscount(
                    createOrderDTO.getCouponId(), totalPrice);
            if (couponDiscount.compareTo(BigDecimal.ZERO) > 0) {
                couponName = "优惠券";
            }
        }

        // 4.2 处理积分抵扣
        Integer usePoints = createOrderDTO.getUsePoints();
        if (usePoints != null && usePoints > 0) {
            Integer userPoints = pointsService.getUserPoints(userId);
            if (userPoints >= usePoints) {
                // 积分抵扣比例：100积分 = 1元
                pointsDiscount = BigDecimal.valueOf(usePoints).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            }
        }

        // 4.3 计算实付金额
        BigDecimal payPrice = totalPrice.subtract(couponDiscount).subtract(pointsDiscount);
        if (payPrice.compareTo(BigDecimal.ZERO) < 0) {
            payPrice = BigDecimal.ZERO;
        }

        // 5. 生成订单号（唯一性保证）
        String orderNo = IdUtil.getSnowflakeNextIdStr();

        // 6. 创建订单记录
        OrderInfoDO orderInfoDO = new OrderInfoDO();
        orderInfoDO.setOrderNo(orderNo);
        orderInfoDO.setUserId(userId);
        orderInfoDO.setTotalPrice(totalPrice);
        orderInfoDO.setPayPrice(payPrice); // 实付金额
        orderInfoDO.setPayType(createOrderDTO.getPayType());
        orderInfoDO.setOrderStatus(1); // 待支付
        orderInfoDO.setDeliveryStatus(0); // 未发货
        orderInfoDO.setReceiverName(address.getConsignee());
        orderInfoDO.setReceiverPhone(address.getPhone());
        orderInfoDO.setReceiverAddress(address.getProvince() + address.getCity() +
            address.getDistrict() + address.getDetailAddress());
        orderInfoDO.setRemark(createOrderDTO.getRemark());

        // 积分和优惠券信息
        orderInfoDO.setCouponId(createOrderDTO.getCouponId());
        orderInfoDO.setCouponName(couponName);
        orderInfoDO.setCouponDiscount(couponDiscount);
        orderInfoDO.setUsePoints(usePoints != null ? usePoints : 0);
        orderInfoDO.setPointsDiscount(pointsDiscount);

        orderInfoMapper.insert(orderInfoDO);
        
        // 6. 创建订单商品项记录
        List<OrderItemVO> orderItems = new ArrayList<>();
        for (CartItemDO cartItem : cartItems) {
            // 获取商品信息
            ProductDO product = productMapper.selectById(cartItem.getProductId());
            if (Objects.isNull(product)) {
                log.warn("商品不存在, productId={}", cartItem.getProductId());
                continue;
            }
            
            // 扣减库存（并发场景下需要考虑库存问题）
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException("A0401", "商品【" + product.getName() + "】库存不足");
            }
            
            // 更新库存
            productService.updateStock(product.getId(), -cartItem.getQuantity());
            
            // 更新销量
            LambdaUpdateWrapper<ProductDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ProductDO::getId, product.getId())
                .set(ProductDO::getSales, product.getSales() + cartItem.getQuantity());
            productMapper.update(null, wrapper);
            
            // 创建订单项
            OrderItemDO orderItemDO = new OrderItemDO();
            orderItemDO.setOrderId(orderInfoDO.getId());
            orderItemDO.setProductId(product.getId());
            orderItemDO.setProductName(product.getName());
            orderItemDO.setProductImage(product.getImage());
            orderItemDO.setPrice(cartItem.getPrice());
            orderItemDO.setQuantity(cartItem.getQuantity());
            orderItemDO.setTotalPrice(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            
            orderItemMapper.insert(orderItemDO);
            
            // 构建订单项VO
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setId(orderItemDO.getId());
            itemVO.setProductId(product.getId());
            itemVO.setProductName(product.getName());
            itemVO.setProductImage(product.getImage());
            itemVO.setPrice(cartItem.getPrice());
            itemVO.setQuantity(cartItem.getQuantity());
            itemVO.setTotalPrice(orderItemDO.getTotalPrice());
            orderItems.add(itemVO);
        }
        
        // 7. 清空购物车
        cartItemMapper.delete(
            new LambdaQueryWrapper<CartItemDO>()
                .eq(CartItemDO::getCartId, createOrderDTO.getCartId())
        );
        
        log.info("创建订单成功, orderId={}, orderNo={}, userId={}", 
            orderInfoDO.getId(), orderNo, userId);
        
        // 8. 返回订单VO
        return this.convertToVO(orderInfoDO, orderItems);
    }
    
    /**
     * 获取订单详情
     */
    @Override
    public OrderVO getOrderDetail(Long orderId, Long userId) {
        OrderInfoDO orderInfoDO = this.getOrderById(orderId, userId);
        
        // 获取订单商品项
        List<OrderItemDO> orderItems = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemDO>()
                .eq(OrderItemDO::getOrderId, orderId)
        );
        
        List<OrderItemVO> itemVOs = orderItems.stream()
            .map(this::convertToItemVO)
            .collect(Collectors.toList());
        
        return this.convertToVO(orderInfoDO, itemVOs);
    }
    
    /**
     * 获取用户订单列表
     *
     * 性能优化：按 user_id 路由到具体分片，单库单表查询，性能最佳。
     */
    @Override
    public Page<OrderVO> getUserOrders(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<OrderInfoDO> page = new Page<>(pageNum, pageSize);

        // 使用 Hint 强制路由到用户所在的分片
        try (HintManager hintManager = ShardingHintUtils.forceRouteByUserId(userId)) {
            LambdaQueryWrapper<OrderInfoDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderInfoDO::getUserId, userId);

            if (Objects.nonNull(status)) {
                wrapper.eq(OrderInfoDO::getOrderStatus, status);
            }

            wrapper.orderByDesc(OrderInfoDO::getCreateTime);

            Page<OrderInfoDO> result = orderInfoMapper.selectPage(page, wrapper);

            // 转换分页结果
            Page<OrderVO> pageResult = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
            pageResult.setRecords(result.getRecords().stream()
                .map(order -> {
                    List<OrderItemDO> items = orderItemMapper.selectList(
                        new LambdaQueryWrapper<OrderItemDO>()
                            .eq(OrderItemDO::getOrderId, order.getId())
                    );
                    return this.convertToVO(order, items.stream()
                        .map(this::convertToItemVO)
                        .collect(Collectors.toList()));
                })
                .collect(Collectors.toList()));

            return pageResult;
        }
    }
    
    /**
     * 获取订单详情（内部使用）
     *
     * 关键：在 ShardingSphere 分片环境下，订单ID 是雪花算法生成的，
     * 与 user_id 没有直接对应关系。需要使用 Hint 机制轮询所有分片。
     *
     * 性能说明：单次查询会扫描所有分片，但只查询一次（轮询查找）。
     * 如果是高频场景，建议订单号中编码 user_id 以加速查询。
     */
    @Override
    public OrderInfoDO getOrderById(Long orderId, Long userId) {
        OrderInfoDO orderInfoDO = null;

        // 方案1：在所有分片中查找订单
        // 由于雪花算法ID无法直接定位分片，需要遍历所有分片
        for (int dbIndex = 0; dbIndex < ShardingKeyUtils.DB_COUNT; dbIndex++) {
            for (int tableIndex = 0; tableIndex < ShardingKeyUtils.TABLE_COUNT; tableIndex++) {
                try (HintManager hintManager = ShardingHintUtils.forceRoute(dbIndex, tableIndex)) {
                    OrderInfoDO temp = orderInfoMapper.selectById(orderId);
                    if (Objects.nonNull(temp)) {
                        orderInfoDO = temp;
                        break;
                    }
                }
            }
            if (Objects.nonNull(orderInfoDO)) {
                break;
            }
        }

        if (Objects.isNull(orderInfoDO)) {
            throw new BusinessException("A0401", "订单不存在");
        }

        // 权限校验（水平权限校验）
        if (!orderInfoDO.getUserId().equals(userId)) {
            throw new BusinessException("A0301", "无权限访问该订单");
        }

        return orderInfoDO;
    }
    
    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean cancelOrder(Long orderId, Long userId) {
        OrderInfoDO orderInfoDO = this.getOrderById(orderId, userId);

        // 只有待支付状态可以取消
        if (orderInfoDO.getOrderStatus() != 1) {
            throw new BusinessException("A0440", "当前状态无法取消订单");
        }

        // 退还优惠券
        if (orderInfoDO.getCouponId() != null) {
            couponService.returnCoupon(orderInfoDO.getCouponId(), userId);
        }

        // 退还积分
        if (orderInfoDO.getUsePoints() != null && orderInfoDO.getUsePoints() > 0) {
            pointsService.addPoints(userId, orderInfoDO.getUsePoints(), 2,
                    "订单 " + orderInfoDO.getOrderNo() + " 取消退还积分");
        }

        // 恢复库存
        List<OrderItemDO> orderItems = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemDO>()
                .eq(OrderItemDO::getOrderId, orderId)
        );

        for (OrderItemDO item : orderItems) {
            // 恢复库存
            productService.updateStock(item.getProductId(), item.getQuantity());

            // 减少销量
            ProductDO product = productMapper.selectById(item.getProductId());
            if (Objects.nonNull(product)) {
                LambdaUpdateWrapper<ProductDO> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(ProductDO::getId, product.getId())
                    .set(ProductDO::getSales, Math.max(0, product.getSales() - item.getQuantity()));
                productMapper.update(null, wrapper);
            }
        }

        // 更新订单状态
        LambdaUpdateWrapper<OrderInfoDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderInfoDO::getId, orderId)
            .set(OrderInfoDO::getOrderStatus, 5); // 已取消

        return orderInfoMapper.update(null, wrapper) > 0;
    }
    
    /**
     * 确认收货
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean confirmReceive(Long orderId, Long userId) {
        OrderInfoDO orderInfoDO = this.getOrderById(orderId, userId);

        // 只有已发货状态可以确认收货
        if (orderInfoDO.getOrderStatus() != 3) {
            throw new BusinessException("A0440", "当前状态无法确认收货");
        }

        LambdaUpdateWrapper<OrderInfoDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderInfoDO::getId, orderId)
            .set(OrderInfoDO::getOrderStatus, 4) // 已完成
            .set(OrderInfoDO::getReceiveTime, LocalDateTime.now());

        return orderInfoMapper.update(null, wrapper) > 0;
    }
    
    /**
     * 删除订单
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean deleteOrder(Long orderId, Long userId) {
        OrderInfoDO orderInfoDO = this.getOrderById(orderId, userId);
        
        // 只有已取消或已完成状态可以删除
        if (orderInfoDO.getOrderStatus() != 4 && orderInfoDO.getOrderStatus() != 5) {
            throw new BusinessException("A0440", "当前状态无法删除订单");
        }
        
        // 删除订单商品项
        orderItemMapper.delete(
            new LambdaQueryWrapper<OrderItemDO>()
                .eq(OrderItemDO::getOrderId, orderId)
        );
        
        // 删除订单（逻辑删除）
        return orderInfoMapper.deleteById(orderId) > 0;
    }
    
    /**
     * 支付订单（模拟）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean payOrder(Long orderId, Long userId) {
        OrderInfoDO orderInfoDO = this.getOrderById(orderId, userId);

        // 只有待支付状态可以支付
        if (orderInfoDO.getOrderStatus() != 1) {
            throw new BusinessException("A0440", "当前状态无法支付");
        }

        LambdaUpdateWrapper<OrderInfoDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderInfoDO::getId, orderId)
            .set(OrderInfoDO::getOrderStatus, 2) // 已支付
            .set(OrderInfoDO::getPayTime, LocalDateTime.now());

        log.info("订单支付成功, orderId={}, orderNo={}, userId={}",
            orderId, orderInfoDO.getOrderNo(), userId);

        // 5. 处理积分和优惠券
        // 5.1 使用优惠券
        if (orderInfoDO.getCouponId() != null) {
            couponService.useCoupon(orderInfoDO.getCouponId(), orderId, userId);
        }

        // 5.2 使用积分
        if (orderInfoDO.getUsePoints() != null && orderInfoDO.getUsePoints() > 0) {
            pointsService.usePoints(userId, orderInfoDO.getUsePoints(), orderId,
                    "订单 " + orderInfoDO.getOrderNo() + " 使用积分");
        }

        // 5.3 发放积分（每消费1元送1积分）
        int gotPoints = orderInfoDO.getPayPrice().intValue();
        if (gotPoints > 0) {
            pointsService.addPoints(userId, gotPoints, 1, orderId,
                    "订单 " + orderInfoDO.getOrderNo() + " 获得积分");
            // 更新订单获得的积分
            orderInfoDO.setGotPoints(gotPoints);
        }

        return orderInfoMapper.update(null, wrapper) > 0;
    }
    
    /**
     * 转换为订单VO
     */
    private OrderVO convertToVO(OrderInfoDO orderInfoDO, List<OrderItemVO> items) {
        OrderVO orderVO = BeanUtil.copyProperties(orderInfoDO, OrderVO.class);
        orderVO.setItems(items);

        // 设置支付方式名称
        if (orderInfoDO.getPayType() != null) {
            orderVO.setPayTypeName(orderInfoDO.getPayType() == 1 ? "微信支付" : "支付宝");
        }

        // 设置订单状态名称
        orderVO.setOrderStatusName(this.getOrderStatusName(orderInfoDO.getOrderStatus()));

        // 获取物流信息（如果订单已发货或已完成）
        if (orderInfoDO.getOrderStatus() >= 3) {
            try {
                var expressVO = expressService.getOrderExpress(orderInfoDO.getId(), orderInfoDO.getUserId());
                orderVO.setExpress(expressVO);
            } catch (Exception e) {
                log.debug("获取物流信息失败, orderId={}", orderInfoDO.getId());
            }
        }

        return orderVO;
    }
    
    /**
     * 转换为订单项VO
     */
    private OrderItemVO convertToItemVO(OrderItemDO orderItemDO) {
        OrderItemVO itemVO = new OrderItemVO();
        itemVO.setId(orderItemDO.getId());
        itemVO.setProductId(orderItemDO.getProductId());
        itemVO.setProductName(orderItemDO.getProductName());
        itemVO.setProductImage(orderItemDO.getProductImage());
        itemVO.setPrice(orderItemDO.getPrice());
        itemVO.setQuantity(orderItemDO.getQuantity());
        itemVO.setTotalPrice(orderItemDO.getTotalPrice());
        return itemVO;
    }
    
    /**
     * 获取订单状态名称
     */
    private String getOrderStatusName(Integer status) {
        return switch (status) {
            case 1 -> "待支付";
            case 2 -> "已支付";
            case 3 -> "已发货";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> "未知状态";
        };
    }

    // ==================== 管理员/商家接口实现 ====================

    /**
     * 后台分页查询订单
     */
    @Override
    public Page<AdminOrderVO> getAdminOrderPage(AdminOrderQueryDTO queryDTO) {
        Page<OrderInfoDO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<OrderInfoDO> wrapper = new LambdaQueryWrapper<>();

        // 按订单号查询
        if (queryDTO.getOrderNo() != null && !queryDTO.getOrderNo().isEmpty()) {
            wrapper.eq(OrderInfoDO::getOrderNo, queryDTO.getOrderNo());
        }

        // 按用户ID查询
        if (queryDTO.getUserId() != null) {
            wrapper.eq(OrderInfoDO::getUserId, queryDTO.getUserId());
        }

        // 按状态查询
        if (queryDTO.getOrderStatus() != null) {
            wrapper.eq(OrderInfoDO::getOrderStatus, queryDTO.getOrderStatus());
        }

        // 按时间范围查询
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(OrderInfoDO::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(OrderInfoDO::getCreateTime, queryDTO.getEndTime());
        }

        wrapper.orderByDesc(OrderInfoDO::getCreateTime);

        Page<OrderInfoDO> result = orderInfoMapper.selectPage(page, wrapper);

        // 转换结果
        Page<AdminOrderVO> pageResult = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        pageResult.setRecords(result.getRecords().stream().map(this::convertToAdminOrderVO).collect(Collectors.toList()));

        return pageResult;
    }

    /**
     * 获取订单统计
     */
    @Override
    public OrderStatsVO getOrderStats() {
        OrderStatsVO stats = new OrderStatsVO();

        Long total = orderInfoMapper.selectCount(null);
        stats.setTotal(total);
        stats.setPending(orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfoDO>().eq(OrderInfoDO::getOrderStatus, 1)));
        stats.setPaid(orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfoDO>().eq(OrderInfoDO::getOrderStatus, 2)));
        stats.setShipped(orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfoDO>().eq(OrderInfoDO::getOrderStatus, 3)));
        stats.setCompleted(orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfoDO>().eq(OrderInfoDO::getOrderStatus, 4)));
        stats.setCancelled(orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfoDO>().eq(OrderInfoDO::getOrderStatus, 5)));

        return stats;
    }

    /**
     * 后台发货
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean shipOrder(Long orderId, String companyCode, String companyName, String trackingNo) {
        // 查询订单
        OrderInfoDO orderInfoDO = null;
        for (int dbIndex = 0; dbIndex < ShardingKeyUtils.DB_COUNT; dbIndex++) {
            for (int tableIndex = 0; tableIndex < ShardingKeyUtils.TABLE_COUNT; tableIndex++) {
                try (HintManager hintManager = ShardingHintUtils.forceRoute(dbIndex, tableIndex)) {
                    OrderInfoDO temp = orderInfoMapper.selectById(orderId);
                    if (temp != null) {
                        orderInfoDO = temp;
                        break;
                    }
                }
            }
            if (orderInfoDO != null) break;
        }

        if (orderInfoDO == null) {
            throw new BusinessException("A0401", "订单不存在");
        }

        // 只有已支付状态可以发货
        if (orderInfoDO.getOrderStatus() != 2) {
            throw new BusinessException("A0440", "当前状态无法发货");
        }

        // 更新订单状态
        LambdaUpdateWrapper<OrderInfoDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderInfoDO::getId, orderId)
            .set(OrderInfoDO::getOrderStatus, 3) // 已发货
            .set(OrderInfoDO::getDeliveryStatus, 1)
            .set(OrderInfoDO::getDeliveryTime, LocalDateTime.now());

        // 创建物流信息
        expressService.shipOrder(orderId);

        return orderInfoMapper.update(null, wrapper) > 0;
    }

    /**
     * 后台取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean adminCancelOrder(Long orderId) {
        // 查询订单
        OrderInfoDO orderInfoDO = null;
        for (int dbIndex = 0; dbIndex < ShardingKeyUtils.DB_COUNT; dbIndex++) {
            for (int tableIndex = 0; tableIndex < ShardingKeyUtils.TABLE_COUNT; tableIndex++) {
                try (HintManager hintManager = ShardingHintUtils.forceRoute(dbIndex, tableIndex)) {
                    OrderInfoDO temp = orderInfoMapper.selectById(orderId);
                    if (temp != null) {
                        orderInfoDO = temp;
                        break;
                    }
                }
            }
            if (orderInfoDO != null) break;
        }

        if (orderInfoDO == null) {
            throw new BusinessException("A0401", "订单不存在");
        }

        // 只有待支付或已支付状态可以取消
        if (orderInfoDO.getOrderStatus() != 1 && orderInfoDO.getOrderStatus() != 2) {
            throw new BusinessException("A0440", "当前状态无法取消订单");
        }

        // 退还优惠券
        if (orderInfoDO.getCouponId() != null) {
            couponService.returnCoupon(orderInfoDO.getCouponId(), orderInfoDO.getUserId());
        }

        // 退还积分
        if (orderInfoDO.getUsePoints() != null && orderInfoDO.getUsePoints() > 0) {
            pointsService.addPoints(orderInfoDO.getUserId(), orderInfoDO.getUsePoints(), 2,
                    "管理员取消订单 " + orderInfoDO.getOrderNo() + " 退还积分");
        }

        // 恢复库存
        List<OrderItemDO> orderItems = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemDO>()
                .eq(OrderItemDO::getOrderId, orderId)
        );

        for (OrderItemDO item : orderItems) {
            productService.updateStock(item.getProductId(), item.getQuantity());

            ProductDO product = productMapper.selectById(item.getProductId());
            if (product != null) {
                LambdaUpdateWrapper<ProductDO> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(ProductDO::getId, product.getId())
                    .set(ProductDO::getSales, Math.max(0, product.getSales() - item.getQuantity()));
                productMapper.update(null, updateWrapper);
            }
        }

        // 更新订单状态
        LambdaUpdateWrapper<OrderInfoDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderInfoDO::getId, orderId)
            .set(OrderInfoDO::getOrderStatus, 5);

        return orderInfoMapper.update(null, wrapper) > 0;
    }

    /**
     * 转换为管理员订单VO
     */
    private AdminOrderVO convertToAdminOrderVO(OrderInfoDO orderInfoDO) {
        AdminOrderVO vo = new AdminOrderVO();
        vo.setId(orderInfoDO.getId());
        vo.setOrderNo(orderInfoDO.getOrderNo());
        vo.setUserId(orderInfoDO.getUserId());
        vo.setTotalPrice(orderInfoDO.getTotalPrice());
        vo.setPayPrice(orderInfoDO.getPayPrice());
        vo.setPayType(orderInfoDO.getPayType());
        vo.setPayTypeName(orderInfoDO.getPayType() != null ?
            (orderInfoDO.getPayType() == 1 ? "微信支付" : "支付宝") : null);
        vo.setOrderStatus(orderInfoDO.getOrderStatus());
        vo.setOrderStatusName(getOrderStatusName(orderInfoDO.getOrderStatus()));
        vo.setReceiverName(orderInfoDO.getReceiverName());
        vo.setReceiverPhone(orderInfoDO.getReceiverPhone());
        vo.setReceiverAddress(orderInfoDO.getReceiverAddress());
        vo.setRemark(orderInfoDO.getRemark());
        vo.setCreateTime(orderInfoDO.getCreateTime());
        vo.setPayTime(orderInfoDO.getPayTime());

        // 获取用户昵称
        if (orderInfoDO.getUserId() != null) {
            UserDO user = userMapper.selectById(orderInfoDO.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }

        // 获取订单商品
        List<OrderItemDO> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemDO>()
                .eq(OrderItemDO::getOrderId, orderInfoDO.getId())
        );

        List<OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setProductId(item.getProductId());
            itemVO.setProductName(item.getProductName());
            itemVO.setProductImage(item.getProductImage());
            itemVO.setPrice(item.getPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setTotalPrice(item.getTotalPrice());
            return itemVO;
        }).collect(Collectors.toList());

        vo.setItems(itemVOs);
        vo.setTotalCount(items.stream().mapToInt(OrderItemDO::getQuantity).sum());

        return vo;
    }
}
