package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券表实体类
 *
 * @author xiu
 * @date 2026/09/03
 */
@Data
@TableName("user_coupon")
public class UserCouponDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户优惠券ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 优惠券模板ID
     */
    private Long templateId;

    /**
     * 优惠券名称（冗余）
     */
    private String couponName;

    /**
     * 类型：1-满减，2-折扣，3-无门槛
     */
    private Integer couponType;

    /**
     * 优惠值（冗余）
     */
    private BigDecimal discountValue;

    /**
     * 使用门槛（冗余）
     */
    private BigDecimal minAmount;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态：0-未使用，1-已使用，2-已过期
     */
    private Integer status;

    /**
     * 使用该优惠券的订单ID
     */
    private Long usedOrderId;

    /**
     * 使用时间
     */
    private LocalDateTime usedTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ========== 辅助字段（非数据库字段）==========

    /**
     * 模板信息（JOIN查询时使用）
     */
    @TableField(exist = false)
    private CouponTemplateDO template;

    /**
     * 是否可用
     */
    @TableField(exist = false)
    private Boolean usable;
}
