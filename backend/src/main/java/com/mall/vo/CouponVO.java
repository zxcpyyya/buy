package com.mall.vo;

import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券VO
 *
 * @author mall
 */
@Data
public class CouponVO {

    private Long id;

    /**
     * 模板ID（领取时）
     */
    private Long templateId;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 类型：1-满减，2-折扣，3-无门槛
     */
    private Integer couponType;

    /**
     * 类型文字描述
     */
    private String couponTypeName;

    /**
     * 优惠值
     */
    private BigDecimal discountValue;

    /**
     * 使用门槛
     */
    private BigDecimal minAmount;

    /**
     * 有效期开始
     */
    private LocalDateTime startTime;

    /**
     * 有效期结束
     */
    private LocalDateTime endTime;

    /**
     * 状态：0-未使用，1-已使用，2-已过期
     */
    private Integer status;

    /**
     * 状态文字描述
     */
    private String statusName;

    /**
     * 是否可用
     */
    private Boolean usable;

    /**
     * 剩余有效天数
     */
    private Integer remainDays;

    /**
     * 领取时间
     */
    private LocalDateTime createTime;
}
