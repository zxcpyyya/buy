package com.mall.service;

import com.mall.dto.CouponTemplateDTO;
import com.mall.vo.CouponVO;

import java.util.List;

/**
 * 优惠券服务接口
 *
 * @author xiu
 */
public interface CouponService {

    /**
     * 获取用户优惠券列表
     *
     * @param userId   用户ID
     * @param status   状态：null-全部，0-未使用，1-已使用，2-已过期
     * @return 优惠券列表
     */
    List<CouponVO> getUserCoupons(Long userId, Integer status);

    /**
     * 获取用户可用的优惠券（订单确认页使用）
     *
     * @param userId     用户ID
     * @param orderAmount 订单金额
     * @return 可用优惠券列表
     */
    List<CouponVO> getAvailableCoupons(Long userId, java.math.BigDecimal orderAmount);

    /**
     * 领取优惠券
     *
     * @param templateId 模板ID
     * @param userId     用户ID
     * @return 领取的用户优惠券ID
     */
    Long receiveCoupon(Long templateId, Long userId);

    /**
     * 使用优惠券（订单支付时调用）
     *
     * @param couponId  用户优惠券ID
     * @param orderId   订单ID
     * @param userId    用户ID
     * @return 是否成功
     */
    Boolean useCoupon(Long couponId, Long orderId, Long userId);

    /**
     * 退还优惠券（订单取消时调用）
     *
     * @param couponId 用户优惠券ID
     * @param userId   用户ID
     * @return 是否成功
     */
    Boolean returnCoupon(Long couponId, Long userId);

    /**
     * 计算优惠金额
     *
     * @param couponId   优惠券ID
     * @param orderAmount 订单金额
     * @return 优惠金额
     */
    java.math.BigDecimal calculateDiscount(Long couponId, java.math.BigDecimal orderAmount);

    /**
     * 获取可领取的优惠券列表
     *
     * @param userId 用户ID
     * @return 模板列表
     */
    List<CouponVO> getAvailableTemplates(Long userId);

    /**
     * 创建优惠券模板（管理员）
     *
     * @param dto 模板信息
     * @return 模板ID
     */
    Long createTemplate(CouponTemplateDTO dto);

    /**
     * 更新优惠券模板（管理员）
     *
     * @param id  模板ID
     * @param dto 模板信息
     * @return 是否成功
     */
    Boolean updateTemplate(Long id, CouponTemplateDTO dto);

    /**
     * 获取优惠券模板详情
     *
     * @param id 模板ID
     * @return 模板信息
     */
    CouponVO getTemplateDetail(Long id);

    /**
     * 获取用户优惠券数量
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 数量
     */
    int getUserCouponCount(Long userId, Integer status);
}
