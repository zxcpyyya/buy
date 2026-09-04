package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物流信息表实体类
 *
 * @author xiu
 * @date 2024/01/01
 */
@Data
@TableName("express")
public class ExpressDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 物流ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 快递单号
     */
    private String expressNo;

    /**
     * 快递公司编码
     */
    private String companyCode;

    /**
     * 快递公司名称
     */
    private String companyName;

    /**
     * 状态：0-待发货，1-运输中，2-派送中，3-已签收，4-拒收/退回
     */
    private Integer status;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

    /**
     * 收货人
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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
