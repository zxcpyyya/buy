package com.mall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.dto.CouponTemplateDTO;
import com.mall.entity.CouponTemplateDO;
import com.mall.entity.UserCouponDO;
import com.mall.mapper.CouponTemplateMapper;
import com.mall.mapper.UserCouponMapper;
import com.mall.service.CouponService;
import com.mall.vo.CouponVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现类
 *
 * @author xiu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponTemplateMapper couponTemplateMapper;
    private final UserCouponMapper userCouponMapper;

    // ========== 用户优惠券相关 ==========

    @Override
    public List<CouponVO> getUserCoupons(Long userId, Integer status) {
        LambdaQueryWrapper<UserCouponDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCouponDO::getUserId, userId);

        if (status != null) {
            wrapper.eq(UserCouponDO::getStatus, status);
        }

        wrapper.orderByDesc(UserCouponDO::getCreateTime);

        List<UserCouponDO> userCoupons = userCouponMapper.selectList(wrapper);
        return userCoupons.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CouponVO> getAvailableCoupons(Long userId, BigDecimal orderAmount) {
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<UserCouponDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCouponDO::getUserId, userId)
                .eq(UserCouponDO::getStatus, 0) // 未使用
                .le(UserCouponDO::getStartTime, now) // 已开始
                .ge(UserCouponDO::getEndTime, now); // 未过期

        List<UserCouponDO> userCoupons = userCouponMapper.selectList(wrapper);

        return userCoupons.stream()
                .filter(coupon -> {
                    // 检查是否满足使用门槛
                    if (coupon.getMinAmount() != null &&
                            coupon.getMinAmount().compareTo(BigDecimal.ZERO) > 0 &&
                            orderAmount.compareTo(coupon.getMinAmount()) < 0) {
                        return false;
                    }
                    return true;
                })
                .map(coupon -> {
                    CouponVO vo = convertToVO(coupon);
                    vo.setUsable(true);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long receiveCoupon(Long templateId, Long userId) {
        // 1. 检查模板是否存在
        CouponTemplateDO template = couponTemplateMapper.selectById(templateId);
        if (Objects.isNull(template)) {
            throw new BusinessException("C0401", "优惠券模板不存在");
        }

        // 2. 检查模板是否启用
        if (template.getStatus() != 1) {
            throw new BusinessException("C0401", "优惠券已禁用");
        }

        // 3. 检查发行量
        if (template.getTotalCount() > 0 &&
                template.getReceivedCount() >= template.getTotalCount()) {
            throw new BusinessException("C0401", "优惠券已领完");
        }

        // 4. 检查有效期
        LocalDateTime now = LocalDateTime.now();
        if (template.getValidType() == 1) {
            if (template.getEndTime() != null && template.getEndTime().isBefore(now)) {
                throw new BusinessException("C0401", "优惠券已过期");
            }
        }

        // 5. 创建用户优惠券（先插入，利用唯一索引防止重复领取）
        UserCouponDO userCoupon = new UserCouponDO();
        userCoupon.setUserId(userId);
        userCoupon.setTemplateId(templateId);
        userCoupon.setCouponName(template.getName());
        userCoupon.setCouponType(template.getCouponType());
        userCoupon.setDiscountValue(template.getDiscountValue());
        userCoupon.setMinAmount(template.getMinAmount());

        // 计算有效期
        if (template.getValidType() == 1) {
            userCoupon.setStartTime(template.getStartTime());
            userCoupon.setEndTime(template.getEndTime());
        } else {
            userCoupon.setStartTime(now);
            userCoupon.setEndTime(now.plusDays(template.getValidDays()));
        }

        userCoupon.setStatus(0); // 未使用

        try {
            userCouponMapper.insert(userCoupon);
        } catch (Exception e) {
            // 唯一索引冲突，说明已领取过
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                throw new BusinessException("C0401", "您已领取过该优惠券");
            }
            throw e;
        }

        // 6. 更新模板领取数量（乐观更新，防止超领）
        int updated = couponTemplateMapper.update(null,
                new LambdaUpdateWrapper<CouponTemplateDO>()
                        .eq(CouponTemplateDO::getId, templateId)
                        .gt(CouponTemplateDO::getReceivedCount, 0) // 确保不会超领
                        .set(CouponTemplateDO::getReceivedCount, template.getReceivedCount() + 1)
        );

        if (updated == 0) {
            // 更新失败，说明已领完，回滚
            throw new BusinessException("C0401", "优惠券已领完");
        }

        log.info("用户领取优惠券成功, userId={}, templateId={}, userCouponId={}",
                userId, templateId, userCoupon.getId());

        return userCoupon.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean useCoupon(Long couponId, Long orderId, Long userId) {
        UserCouponDO coupon = userCouponMapper.selectById(couponId);
        if (Objects.isNull(coupon)) {
            throw new BusinessException("C0401", "优惠券不存在");
        }

        if (!coupon.getUserId().equals(userId)) {
            throw new BusinessException("C0301", "无权限使用该优惠券");
        }

        if (coupon.getStatus() != 0) {
            throw new BusinessException("C0401", "优惠券不可用");
        }

        if (coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("C0401", "优惠券已过期");
        }

        // 更新为已使用
        coupon.setStatus(1);
        coupon.setUsedOrderId(orderId);
        coupon.setUsedTime(LocalDateTime.now());
        userCouponMapper.updateById(coupon);

        log.info("优惠券已使用, couponId={}, orderId={}", couponId, orderId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean returnCoupon(Long couponId, Long userId) {
        UserCouponDO coupon = userCouponMapper.selectById(couponId);
        if (Objects.isNull(coupon)) {
            return false;
        }

        if (!coupon.getUserId().equals(userId)) {
            return false;
        }

        if (coupon.getStatus() != 1) {
            return false;
        }

        // 退还为未使用
        coupon.setStatus(0);
        coupon.setUsedOrderId(null);
        coupon.setUsedTime(null);
        userCouponMapper.updateById(coupon);

        log.info("优惠券已退还, couponId={}", couponId);
        return true;
    }

    @Override
    public BigDecimal calculateDiscount(Long couponId, BigDecimal orderAmount) {
        if (couponId == null) {
            return BigDecimal.ZERO;
        }

        UserCouponDO coupon = userCouponMapper.selectById(couponId);
        if (Objects.isNull(coupon) || coupon.getStatus() != 0) {
            return BigDecimal.ZERO;
        }

        // 检查是否满足门槛
        if (coupon.getMinAmount() != null &&
                coupon.getMinAmount().compareTo(BigDecimal.ZERO) > 0 &&
                orderAmount.compareTo(coupon.getMinAmount()) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        switch (coupon.getCouponType()) {
            case 1 -> { // 满减
                discount = coupon.getDiscountValue();
            }
            case 2 -> { // 折扣
                discount = orderAmount.multiply(
                        BigDecimal.ONE.subtract(coupon.getDiscountValue())
                );
            }
            case 3 -> { // 无门槛
                discount = coupon.getDiscountValue();
            }
            default -> discount = BigDecimal.ZERO;
        }

        // 优惠金额不能超过订单金额
        return discount.min(orderAmount);
    }

    @Override
    public List<CouponVO> getAvailableTemplates(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<CouponTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponTemplateDO::getStatus, 1)
                .le(CouponTemplateDO::getStartTime, now)
                .and(w -> w
                        .isNull(CouponTemplateDO::getEndTime)
                        .or()
                        .ge(CouponTemplateDO::getEndTime, now)
                )
                .and(w -> w
                        .eq(CouponTemplateDO::getTotalCount, 0)
                        .or()
                        .apply("total_count = 0 OR received_count < total_count")
                );

        List<CouponTemplateDO> templates = couponTemplateMapper.selectList(wrapper);
        return templates.stream()
                .map(template -> {
                    CouponVO vo = BeanUtil.copyProperties(template, CouponVO.class);
                    vo.setTemplateId(template.getId());
                    vo.setName(template.getName());

                    // 检查用户是否已领取
                    LambdaQueryWrapper<UserCouponDO> userWrapper = new LambdaQueryWrapper<>();
                    userWrapper.eq(UserCouponDO::getUserId, userId)
                            .eq(UserCouponDO::getTemplateId, template.getId());
                    long count = userCouponMapper.selectCount(userWrapper);

                    // 检查是否可领取
                    boolean canReceive = template.getTotalCount() == 0 ||
                            template.getReceivedCount() < template.getTotalCount();
                    boolean userNotReceived = count < template.getPerUserLimit();
                    vo.setUsable(canReceive && userNotReceived);

                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Long createTemplate(CouponTemplateDTO dto) {
        CouponTemplateDO template = BeanUtil.copyProperties(dto, CouponTemplateDO.class);
        couponTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    public Boolean updateTemplate(Long id, CouponTemplateDTO dto) {
        CouponTemplateDO template = couponTemplateMapper.selectById(id);
        if (Objects.isNull(template)) {
            throw new BusinessException("C0401", "优惠券模板不存在");
        }

        BeanUtil.copyProperties(dto, template);
        return couponTemplateMapper.updateById(template) > 0;
    }

    @Override
    public CouponVO getTemplateDetail(Long id) {
        CouponTemplateDO template = couponTemplateMapper.selectById(id);
        if (Objects.isNull(template)) {
            throw new BusinessException("C0401", "优惠券模板不存在");
        }

        CouponVO vo = BeanUtil.copyProperties(template, CouponVO.class);
        vo.setTemplateId(template.getId());
        vo.setName(template.getName());
        return vo;
    }

    @Override
    public int getUserCouponCount(Long userId, Integer status) {
        LambdaQueryWrapper<UserCouponDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCouponDO::getUserId, userId);

        if (status != null) {
            wrapper.eq(UserCouponDO::getStatus, status);
        }

        return (int) userCouponMapper.selectCount(wrapper);
    }

    // ========== 私有方法 ==========

    private CouponVO convertToVO(UserCouponDO coupon) {
        CouponVO vo = BeanUtil.copyProperties(coupon, CouponVO.class);
        vo.setTemplateId(coupon.getTemplateId());
        vo.setName(coupon.getCouponName());

        // 类型名称
        vo.setCouponTypeName(getCouponTypeName(coupon.getCouponType()));

        // 状态名称
        vo.setStatusName(getStatusName(coupon.getStatus()));

        // 检查是否可用
        LocalDateTime now = LocalDateTime.now();
        boolean isUsable = coupon.getStatus() == 0 &&
                coupon.getStartTime().isBefore(now) &&
                coupon.getEndTime().isAfter(now);
        vo.setUsable(isUsable);

        // 剩余天数
        if (coupon.getEndTime() != null && coupon.getEndTime().isAfter(now)) {
            vo.setRemainDays((int) ChronoUnit.DAYS.between(now, coupon.getEndTime()));
        }

        return vo;
    }

    private String getCouponTypeName(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "满减券";
            case 2 -> "折扣券";
            case 3 -> "无门槛券";
            default -> "未知";
        };
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "未使用";
            case 1 -> "已使用";
            case 2 -> "已过期";
            default -> "未知";
        };
    }
}
