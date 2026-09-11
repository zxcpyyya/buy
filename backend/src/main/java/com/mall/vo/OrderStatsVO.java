package com.mall.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 订单统计VO
 *
 * @author xiu
 */
@Data
public class OrderStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总订单数
     */
    private Long total;

    /**
     * 待支付数
     */
    private Long pending;

    /**
     * 已支付数
     */
    private Long paid;

    /**
     * 已发货数
     */
    private Long shipped;

    /**
     * 已完成数
     */
    private Long completed;

    /**
     * 已取消数
     */
    private Long cancelled;
}
