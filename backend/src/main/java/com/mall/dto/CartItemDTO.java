package com.mall.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车项DTO
 * 
 * @author xiu
 * @date 2024/01/01
 */
@Data
public class CartItemDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 商品价格
     */
    private BigDecimal price;
    
    /**
     * 商品图片
     */
    private String image;
    
    /**
     * 小计金额
     */
    private BigDecimal subtotal;
}
