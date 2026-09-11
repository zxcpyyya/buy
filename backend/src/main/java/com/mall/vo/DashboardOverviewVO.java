package com.mall.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Dashboard 统计概览VO
 *
 * @author xiu
 */
@Data
public class DashboardOverviewVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 今日订单数
     */
    private Long todayOrders;

    /**
     * 今日销售额
     */
    private BigDecimal todaySales;

    /**
     * 新增用户数
     */
    private Long newUsers;

    /**
     * 库存预警商品数
     */
    private Long lowStockProducts;

    /**
     * 订单增长率
     */
    private BigDecimal orderGrowth;

    /**
     * 销售增长率
     */
    private BigDecimal salesGrowth;

    /**
     * 用户增长率
     */
    private BigDecimal userGrowth;
}
