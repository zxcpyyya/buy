package com.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建订单请求DTO
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Data
public class CreateOrderDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 地址ID
     */
    @NotNull(message = "收货地址不能为空")
    private Long addressId;
    
    /**
     * 支付方式：1-微信，2-支付宝
     */
    @NotNull(message = "支付方式不能为空")
    private Integer payType;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 购物车ID（从购物车下单时使用）
     */
    private Long cartId;

    // ========== 优惠券/积分 ==========

    /**
     * 使用的优惠券ID（可选）
     */
    private Long couponId;

    /**
     * 使用积分抵扣（可选，默认0）
     */
    private Integer usePoints;
}
