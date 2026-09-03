package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车商品项表实体类
 * 
 * @author mall
 * @date 2024/01/01
 */
@Data
@TableName("cart_item")
public class CartItemDO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 购物车商品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 购物车ID
     */
    private Long cartId;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 商品单价
     */
    private BigDecimal price;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 删除标记
     */
    @TableLogic
    private Integer deleted;
}
