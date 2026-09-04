package com.mall.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车商品项VO
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Data
public class CartItemVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 购物车商品ID
     */
    private Long id;
    
    /**
     * 商品ID
     */
    private Long productId;
    
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
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 小计金额
     */
    private BigDecimal subtotal;
    
    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 是否选中
     */
    private Boolean selected;
}
