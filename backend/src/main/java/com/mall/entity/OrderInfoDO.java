package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表实体类
 * 
 * @author xiu
 * @date 2024/01/01
 */
@Data
@TableName("order_info")
public class OrderInfoDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     *
     * 重要：使用雪花算法分布式主键，不能用 IdType.AUTO
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID（分片键）
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 订单总价
     */
    private BigDecimal totalPrice;
    
    /**
     * 实付金额
     */
    private BigDecimal payPrice;
    
    /**
     * 支付方式：1-微信，2-支付宝
     */
    private Integer payType;
    
    /**
     * 订单状态：1-待支付，2-已支付，3-已发货，4-已完成，5-已取消
     */
    private Integer orderStatus;
    
    /**
     * 配送状态：0-未发货，1-已发货
     */
    private Integer deliveryStatus;
    
    /**
     * 收货人姓名
     */
    private String receiverName;
    
    /**
     * 收货人电话
     */
    private String receiverPhone;
    
    /**
     * 收货地址
     */
    private String receiverAddress;
    
    /**
     * 备注
     */
    private String remark;

    // ========== 积分/优惠券 ==========

    /**
     * 使用积分数量
     */
    private Integer usePoints;

    /**
     * 积分抵扣金额
     */
    private BigDecimal pointsDiscount;

    /**
     * 使用的优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券抵扣金额
     */
    private BigDecimal couponDiscount;

    /**
     * 获得积分数量
     */
    private Integer gotPoints;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;
    
    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;
    
    /**
     * 收货时间
     */
    private LocalDateTime receiveTime;
    
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
