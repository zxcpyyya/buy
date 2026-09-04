package com.mall.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车VO
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Data
public class CartVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 购物车ID
     */
    private Long id;
    
    /**
     * 购物车商品列表
     */
    private List<CartItemVO> items;
    
    /**
     * 总价
     */
    private BigDecimal totalPrice;
    
    /**
     * 商品总数量
     */
    private Integer totalCount;
    
    /**
     * 是否全选
     */
    private Boolean allSelected;
}
