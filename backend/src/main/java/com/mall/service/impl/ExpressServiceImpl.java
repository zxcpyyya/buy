package com.mall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.dto.ExpressCreateDTO;
import com.mall.entity.ExpressDO;
import com.mall.entity.ExpressTraceDO;
import com.mall.entity.OrderInfoDO;
import com.mall.mapper.ExpressMapper;
import com.mall.mapper.ExpressTraceMapper;
import com.mall.mapper.OrderInfoMapper;
import com.mall.service.ExpressService;
import com.mall.vo.ExpressTraceVO;
import com.mall.vo.ExpressVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 物流服务实现类
 *
 * @author xiu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpressServiceImpl implements ExpressService {

    private final ExpressMapper expressMapper;
    private final ExpressTraceMapper expressTraceMapper;
    private final OrderInfoMapper orderInfoMapper;

    // 快递公司编码映射
    private static final java.util.Map<String, String> EXPRESS_COMPANIES = new java.util.HashMap<>();
    private static final java.util.Map<Integer, String> STATUS_NAMES = new java.util.HashMap<>();

    static {
        EXPRESS_COMPANIES.put("SF", "顺丰速运");
        EXPRESS_COMPANIES.put("YTO", "圆通速递");
        EXPRESS_COMPANIES.put("ZTO", "中通快递");
        EXPRESS_COMPANIES.put("YD", "韵达快递");
        EXPRESS_COMPANIES.put("STO", "申通快递");
        EXPRESS_COMPANIES.put("JD", "京东物流");
        EXPRESS_COMPANIES.put("EMS", "EMS");
        EXPRESS_COMPANIES.put("YZPY", "中国邮政");

        STATUS_NAMES.put(0, "待发货");
        STATUS_NAMES.put(1, "运输中");
        STATUS_NAMES.put(2, "派送中");
        STATUS_NAMES.put(3, "已签收");
        STATUS_NAMES.put(4, "拒收/退回");
    }

    @Override
    public ExpressVO getOrderExpress(Long orderId, Long userId) {
        LambdaQueryWrapper<ExpressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpressDO::getOrderId, orderId);

        ExpressDO express = expressMapper.selectOne(wrapper);
        if (Objects.isNull(express)) {
            return null;
        }

        return convertToVO(express);
    }

    @Override
    public ExpressVO getExpressDetail(Long expressId, Long userId) {
        ExpressDO express = expressMapper.selectById(expressId);
        if (Objects.isNull(express)) {
            throw new BusinessException("E0401", "物流信息不存在");
        }

        ExpressVO vo = convertToVO(express);

        // 查询轨迹
        LambdaQueryWrapper<ExpressTraceDO> traceWrapper = new LambdaQueryWrapper<>();
        traceWrapper.eq(ExpressTraceDO::getExpressId, expressId)
                .orderByAsc(ExpressTraceDO::getTraceTime);

        List<ExpressTraceDO> traces = expressTraceMapper.selectList(traceWrapper);
        vo.setTraces(traces.stream()
                .map(this::convertTraceToVO)
                .collect(Collectors.toList()));

        // 设置最新轨迹
        if (!CollectionUtils.isEmpty(traces)) {
            vo.setLastTrace(convertTraceToVO(traces.get(traces.size() - 1)));
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createExpress(ExpressCreateDTO dto, Long userId) {
        // 检查订单是否存在
        OrderInfoDO order = orderInfoMapper.selectById(dto.getOrderId());
        if (Objects.isNull(order)) {
            throw new BusinessException("E0401", "订单不存在");
        }

        // 检查是否已有物流
        ExpressDO existExpress = expressMapper.selectOne(
                new LambdaQueryWrapper<ExpressDO>().eq(ExpressDO::getOrderId, dto.getOrderId())
        );
        if (Objects.nonNull(existExpress)) {
            throw new BusinessException("E0401", "该订单已有物流信息");
        }

        ExpressDO express = new ExpressDO();
        express.setOrderId(dto.getOrderId());
        express.setExpressNo(dto.getExpressNo());
        express.setCompanyCode(dto.getCompanyCode());
        express.setCompanyName(dto.getCompanyName());
        express.setStatus(1); // 运输中
        express.setShipTime(LocalDateTime.now());
        express.setReceiverName(dto.getReceiverName());
        express.setReceiverPhone(dto.getReceiverPhone());
        express.setReceiverAddress(dto.getReceiverAddress());

        expressMapper.insert(express);

        // 创建初始轨迹
        createExpressTrace(express.getId(), 1, "包裹已发出", "商家发货地");

        log.info("创建物流信息成功, expressId={}, orderId={}", express.getId(), dto.getOrderId());

        return express.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateExpressStatus(Long expressId, Integer status, String message, String location) {
        ExpressDO express = expressMapper.selectById(expressId);
        if (Objects.isNull(express)) {
            throw new BusinessException("E0401", "物流信息不存在");
        }

        // 更新状态
        express.setStatus(status);
        expressMapper.updateById(express);

        // 添加轨迹
        String statusName = STATUS_NAMES.getOrDefault(status, "未知状态");
        createExpressTrace(expressId, status, statusName + (message != null ? "：" + message : ""), location);

        log.info("更新物流状态成功, expressId={}, status={}", expressId, status);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean shipOrder(Long orderId) {
        // 检查订单
        OrderInfoDO order = orderInfoMapper.selectById(orderId);
        if (Objects.isNull(order)) {
            throw new BusinessException("E0401", "订单不存在");
        }

        if (order.getOrderStatus() != 2) { // 已支付状态才能发货
            throw new BusinessException("E0401", "订单状态不正确，无法发货");
        }

        // 模拟生成快递单号
        String expressNo = "SF" + System.currentTimeMillis();

        ExpressCreateDTO dto = new ExpressCreateDTO();
        dto.setOrderId(orderId);
        dto.setExpressNo(expressNo);
        dto.setCompanyCode("SF");
        dto.setCompanyName("顺丰速运");
        dto.setReceiverName(order.getReceiverName());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setReceiverAddress(order.getReceiverAddress());

        createExpress(dto, null);

        // 更新订单状态为已发货
        order.setOrderStatus(3); // 已发货
        orderInfoMapper.updateById(order);

        log.info("订单发货成功, orderId={}, expressNo={}", orderId, expressNo);

        return true;
    }

    // ========== 私有方法 ==========

    private ExpressVO convertToVO(ExpressDO express) {
        ExpressVO vo = BeanUtil.copyProperties(express, ExpressVO.class);
        vo.setStatusName(STATUS_NAMES.getOrDefault(express.getStatus(), "未知"));
        return vo;
    }

    private ExpressTraceVO convertTraceToVO(ExpressTraceDO trace) {
        ExpressTraceVO vo = BeanUtil.copyProperties(trace, ExpressTraceVO.class);
        vo.setStatusName(STATUS_NAMES.getOrDefault(trace.getStatus(), "未知"));
        return vo;
    }

    private void createExpressTrace(Long expressId, Integer status, String message, String location) {
        ExpressTraceDO trace = new ExpressTraceDO();
        trace.setExpressId(expressId);
        trace.setStatus(status);
        trace.setStatusName(STATUS_NAMES.getOrDefault(status, "未知"));
        trace.setMessage(message);
        trace.setLocation(location);
        trace.setTraceTime(LocalDateTime.now());

        expressTraceMapper.insert(trace);
    }
}
