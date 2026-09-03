package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单商品项表实体类
 * 
 * @author mall
 * @date 2024/01/01
 */
@Data
@TableName("order_item")
public class OrderItemDO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 订单项ID
     *
     * 重要：使用雪花算法分布式主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID（冗余 + 分片键）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 商品名称（冗余）
     */
    private String productName;
    
    /**
     * 商品图片（冗余）
     */
    private String productImage;
    
    /**
     * 商品单价
     */
    private BigDecimal price;
    
    /**
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 小计
     */
    private BigDecimal totalPrice;
    
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
