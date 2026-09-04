package com.mall.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分记录VO
 *
 * @author xiu
 */
@Data
public class PointsRecordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 积分变动数量
     */
    private Integer points;

    /**
     * 变动后余额
     */
    private Integer balance;

    /**
     * 类型：1-订单获取，2-订单使用，3-活动赠送，4-过期扣除
     */
    private Integer type;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
