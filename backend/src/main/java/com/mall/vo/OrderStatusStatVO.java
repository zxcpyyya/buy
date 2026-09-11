package com.mall.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Dashboard订单状态统计VO
 *
 * @author xiu
 */
@Data
public class OrderStatusStatVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 待支付数量
     */
    private Integer pending;

    /**
     * 处理中数量（已支付）
     */
    private Integer processing;

    /**
     * 已发货数量
     */
    private Integer shipped;

    /**
     * 已完成数量
     */
    private Integer completed;

    /**
     * 已取消数量
     */
    private Integer cancelled;

    /**
     * 总计
     */
    private Integer total;
}
