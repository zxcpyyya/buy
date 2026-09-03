package com.mall.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情VO
 * 
 * @author mall
 * @date 2024/01/01
 */
@Data
public class OrderVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 订单ID
     */
    private Long id;
    
    /**
     * 订单号
     */
    private String orderNo;
    
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
     * 支付方式名称
     */
    private String payTypeName;
    
    /**
     * 订单状态：1-待支付，2-已支付，3-已发货，4-已完成，5-已取消
     */
    private Integer orderStatus;
    
    /**
     * 订单状态名称
     */
    private String orderStatusName;
    
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
    
    /**
     * 订单商品列表
     */
    private List<OrderItemVO> items;
    
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
    private LocalDateTime createTime;
}
