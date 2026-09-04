package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板表实体类
 *
 * @author xiu
 * @date 2024/01/01
 */
@Data
@TableName("coupon_template")
public class CouponTemplateDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 优惠券类型：1-满减券，2-折扣券，3-无门槛券
     */
    private Integer couponType;

    /**
     * 优惠值
     * - 满减券：减免金额
     * - 折扣券：折扣率（如0.8表示8折）
     * - 无门槛券：减免金额
     */
    private BigDecimal discountValue;

    /**
     * 使用门槛金额（0表示无门槛）
     */
    private BigDecimal minAmount;

    /**
     * 发行总量（0表示不限量）
     */
    private Integer totalCount;

    /**
     * 每人限领数量
     */
    private Integer perUserLimit;

    /**
     * 已领取数量
     */
    private Integer receivedCount;

    /**
     * 有效期类型：1-固定日期，2-领取后N天有效
     */
    private Integer validType;

    /**
     * 有效期开始时间
     */
    private LocalDateTime startTime;

    /**
     * 有效期结束时间
     */
    private LocalDateTime endTime;

    /**
     * 领取后有效天数（validType=2时）
     */
    private Integer validDays;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
