package com.mall.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物流信息VO
 *
 * @author xiu
 */
@Data
public class ExpressVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 状态名称
     */
    private String statusName;

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

    /**
     * 最新轨迹
     */
    private ExpressTraceVO lastTrace;

    /**
     * 轨迹列表
     */
    private java.util.List<ExpressTraceVO> traces;
}
