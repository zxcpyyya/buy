package com.mall.service;

import com.mall.vo.DashboardOverviewVO;
import com.mall.vo.OrderStatusStatVO;
import com.mall.vo.RecentOrderVO;
import com.mall.vo.SalesTrendVO;

import java.util.List;

/**
 * Dashboard统计Service
 *
 * @author xiu
 */
public interface DashboardService {

    /**
     * 获取概览统计数据
     */
    DashboardOverviewVO getOverview();

    /**
     * 获取销售趋势
     *
     * @param period 周期：today/week/month/year
     */
    SalesTrendVO getSalesTrend(String period);

    /**
     * 获取订单状态统计
     */
    OrderStatusStatVO getOrderStatusStat();

    /**
     * 获取最新订单
     *
     * @param limit 返回数量
     */
    List<RecentOrderVO> getRecentOrders(Integer limit);
}
