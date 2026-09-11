package com.mall.controller;

import com.mall.common.annotation.RequireLogin;
import com.mall.common.result.Result;
import com.mall.service.DashboardService;
import com.mall.vo.DashboardOverviewVO;
import com.mall.vo.OrderStatusStatVO;
import com.mall.vo.RecentOrderVO;
import com.mall.vo.SalesTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dashboard统计Controller
 *
 * @author xiu
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard统计", description = "后台管理仪表盘统计数据接口")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取概览统计
     */
    @GetMapping("/overview")
    @RequireLogin
    @Operation(summary = "概览统计", description = "获取今日订单、销售额、用户等概览数据")
    public Result<DashboardOverviewVO> getOverview() {
        return Result.success(dashboardService.getOverview());
    }

    /**
     * 获取销售趋势
     */
    @GetMapping("/sales-trend")
    @RequireLogin
    @Operation(summary = "销售趋势", description = "获取销售趋势数据")
    public Result<SalesTrendVO> getSalesTrend(
            @Parameter(description = "周期：today/week/month/year")
            @RequestParam(defaultValue = "week") String period) {
        return Result.success(dashboardService.getSalesTrend(period));
    }

    /**
     * 获取订单状态统计
     */
    @GetMapping("/order-status")
    @RequireLogin
    @Operation(summary = "订单状态统计", description = "获取各状态订单数量分布")
    public Result<OrderStatusStatVO> getOrderStatusStat() {
        return Result.success(dashboardService.getOrderStatusStat());
    }

    /**
     * 获取最新订单
     */
    @GetMapping("/recent-orders")
    @RequireLogin
    @Operation(summary = "最新订单", description = "获取最近的订单列表")
    public Result<List<RecentOrderVO>> getRecentOrders(
            @Parameter(description = "返回数量")
            @RequestParam(defaultValue = "5") Integer limit) {
        return Result.success(dashboardService.getRecentOrders(limit));
    }
}
