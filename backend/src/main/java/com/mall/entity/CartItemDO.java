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
 * @author xiu
 * @date 2024/01/01
 */
@Data
@TableName("cart_item")
public class CartItemDO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 购物车商品ID
     *
     * 重要：广播表在每个分片库都完整复制一份，
     * 如果使用 AUTO 自增，多库ID会重复。使用雪花算法ASSIGN_ID保证全局唯一。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 购物车ID
     */
    private Long cartId;

    /**
     * 用户ID（冗余，便于查询）
     */
    private Long userId;

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
     * 是否选中：0-否，1-是
     */
    private Integer selected;
    
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
