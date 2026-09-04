package com.mall.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品DTO
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Data
public class ProductDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 商品名称
     */
    private String name;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 商品价格
     */
    private BigDecimal price;
    
    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 商品主图
     */
    private String image;
    
    /**
     * 商品图片集
     */
    private String images;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;
}
