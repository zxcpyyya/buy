package com.mall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.entity.PointsRecordDO;
import com.mall.mapper.PointsRecordMapper;
import com.mall.service.PointsService;
import com.mall.vo.PointsRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 积分服务实现类
 *
 * @author mall
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final PointsRecordMapper pointsRecordMapper;

    /**
     * 积分规则：每消费1元 = 1积分
     */
    private static final int POINTS_PER_YUAN = 1;

    @Override
    public Integer getUserPoints(Long userId) {
        Integer balance = pointsRecordMapper.getUserPointsBalance(userId);
        return balance != null ? balance : 0;
    }

    @Override
    public List<PointsRecordVO> getPointsRecords(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<PointsRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsRecordDO::getUserId, userId)
                .orderByDesc(PointsRecordDO::getCreateTime);

        // 分页查询
        int offset = (pageNum - 1) * pageSize;
        wrapper.last("LIMIT " + offset + ", " + pageSize);

        List<PointsRecordDO> records = pointsRecordMapper.selectList(wrapper);
        return records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addPoints(Long userId, Integer points, Integer type, Long orderId, String description) {
        if (points <= 0) {
            throw new BusinessException("P0401", "积分数量必须大于0");
        }

        // 计算当前余额
        Integer currentBalance = getUserPoints(userId);
        int newBalance = currentBalance + points;

        // 记录积分变动
        PointsRecordDO record = new PointsRecordDO();
        record.setUserId(userId);
        record.setPoints(points);
        record.setBalance(newBalance);
        record.setType(type);
        record.setOrderId(orderId);
        record.setDescription(description);

        pointsRecordMapper.insert(record);

        log.info("积分增加成功, userId={}, points={}, newBalance={}, type={}",
                userId, points, newBalance, type);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean usePoints(Long userId, Integer points, Long orderId, String description) {
        if (points <= 0) {
            throw new BusinessException("P0401", "积分数量必须大于0");
        }

        // 检查余额
        Integer currentBalance = getUserPoints(userId);
        if (currentBalance < points) {
            throw new BusinessException("P0401", "积分余额不足");
        }

        int newBalance = currentBalance - points;

        // 记录积分变动（使用积分为负数）
        PointsRecordDO record = new PointsRecordDO();
        record.setUserId(userId);
        record.setPoints(-points); // 使用为负数
        record.setBalance(newBalance);
        record.setType(2); // 订单使用
        record.setOrderId(orderId);
        record.setDescription(description);

        pointsRecordMapper.insert(record);

        log.info("积分使用成功, userId={}, points={}, newBalance={}",
                userId, points, newBalance);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deductPoints(Long userId, Integer points, String description) {
        if (points <= 0) {
            throw new BusinessException("P0401", "积分数量必须大于0");
        }

        Integer currentBalance = getUserPoints(userId);
        if (currentBalance < points) {
            points = currentBalance; // 扣除全部
        }

        int newBalance = currentBalance - points;

        PointsRecordDO record = new PointsRecordDO();
        record.setUserId(userId);
        record.setPoints(-points);
        record.setBalance(newBalance);
        record.setType(4); // 过期扣除
        record.setDescription(description);

        pointsRecordMapper.insert(record);

        log.info("积分扣除成功, userId={}, points={}, newBalance={}",
                userId, points, newBalance);

        return true;
    }

    @Override
    public Integer getTotalPoints(Long userId) {
        LambdaQueryWrapper<PointsRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsRecordDO::getUserId, userId)
                .gt(PointsRecordDO::getPoints, 0);

        List<PointsRecordDO> records = pointsRecordMapper.selectList(wrapper);
        return records.stream()
                .mapToInt(PointsRecordDO::getPoints)
                .sum();
    }

    /**
     * 根据订单金额计算可获得积分
     *
     * @param orderAmount 订单金额
     * @return 可获得积分
     */
    public int calculatePointsByAmount(java.math.BigDecimal orderAmount) {
        return orderAmount.multiply(java.math.BigDecimal.valueOf(POINTS_PER_YUAN)).intValue();
    }

    // ========== 私有方法 ==========

    private PointsRecordVO convertToVO(PointsRecordDO record) {
        PointsRecordVO vo = BeanUtil.copyProperties(record, PointsRecordVO.class);
        vo.setTypeName(getTypeName(record.getType()));
        return vo;
    }

    private String getTypeName(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "订单获取";
            case 2 -> "订单使用";
            case 3 -> "活动赠送";
            case 4 -> "过期扣除";
            default -> "其他";
        };
    }
}
