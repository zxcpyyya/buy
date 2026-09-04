package com.mall.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板DTO
 *
 * @author xiu
 */
@Data
public class CouponTemplateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 优惠券名称
     */
    @NotBlank(message = "优惠券名称不能为空")
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 类型：1-满减，2-折扣，3-无门槛
     */
    @NotNull(message = "优惠券类型不能为空")
    private Integer couponType;

    /**
     * 优惠值
     */
    @NotNull(message = "优惠值不能为空")
    @DecimalMin(value = "0.01", message = "优惠值必须大于0")
    private BigDecimal discountValue;

    /**
     * 使用门槛
     */
    private BigDecimal minAmount = BigDecimal.ZERO;

    /**
     * 发行总量（0表示不限量）
     */
    private Integer totalCount = 0;

    /**
     * 每人限领数量
     */
    private Integer perUserLimit = 1;

    /**
     * 有效期类型：1-固定日期，2-领取后N天有效
     */
    private Integer validType = 1;

    /**
     * 有效期开始时间
     */
    private LocalDateTime startTime;

    /**
     * 有效期结束时间
     */
    private LocalDateTime endTime;

    /**
     * 领取后有效天数
     */
    private Integer validDays;

    /**
     * 状态
     */
    private Integer status = 1;
}
