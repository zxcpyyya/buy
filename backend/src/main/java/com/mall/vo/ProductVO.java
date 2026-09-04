package com.mall.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情VO
 * 
 * @author xiu
 * @date 2024/01/01
 */
@Data
public class ProductVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 商品ID
     */
    private Long id;
    
    /**
     * 商品名称
     */
    private String name;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
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
    private List<String> images;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 销量
     */
    private Integer sales;
    
    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;
}
