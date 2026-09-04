package com.mall.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品查询DTO
 * 
 * @author xiu
 * @date 2024/01/01
 */
@Data
public class ProductQueryDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 关键词
     */
    private String keyword;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 最小价格
     */
    private Long minPrice;
    
    /**
     * 最大价格
     */
    private Long maxPrice;
    
    /**
     * 排序字段
     */
    private String sortBy;
    
    /**
     * 排序方式
     */
    private String sortOrder;
    
    /**
     * 当前页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
