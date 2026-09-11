package com.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.annotation.RequireLogin;
import com.mall.common.result.PageResult;
import com.mall.common.result.Result;
import com.mall.dto.AdminOrderQueryDTO;
import com.mall.service.OrderService;
import com.mall.vo.AdminOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员订单Controller
 *
 * @author xiu
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
@Tag(name = "管理员订单", description = "管理员订单查询、管理接口")
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 分页查询订单列表
     */
    @GetMapping("/list")
    @RequireLogin
    @Operation(summary = "订单列表", description = "管理员分页查询订单列表")
    public Result<PageResult<AdminOrderVO>> getOrderList(AdminOrderQueryDTO queryDTO) {
        log.debug("管理员查询订单列表, queryDTO={}", queryDTO);
        
        Page<AdminOrderVO> page = orderService.getAdminOrderPage(queryDTO);
        
        PageResult<AdminOrderVO> pageResult = PageResult.of(
            page.getRecords(),
            page.getTotal(),
            (int) page.getCurrent(),
            (int) page.getSize()
        );
        
        return Result.success(pageResult);
    }

    /**
     * 获取订单统计
     */
    @GetMapping("/stats")
    @RequireLogin
    @Operation(summary = "订单统计", description = "获取订单状态统计")
    public Result<OrderStatsVO> getOrderStats() {
        OrderStatsVO stats = orderService.getOrderStats();
        return Result.success(stats);
    }

    /**
     * 订单发货
     */
    @PostMapping("/{orderId}/ship")
    @RequireLogin
    @Operation(summary = "订单发货", description = "商家/管理员对订单进行发货")
    public Result<Boolean> shipOrder(
            @Parameter(description = "订单ID")
            @PathVariable Long orderId,
            @Parameter(description = "物流公司编码")
            @RequestParam String companyCode,
            @Parameter(description = "物流公司名称")
            @RequestParam String companyName,
            @Parameter(description = "运单号")
            @RequestParam String trackingNo) {
        log.info("订单发货, orderId={}, company={}, trackingNo={}", orderId, companyCode, trackingNo);
        
        Boolean result = orderService.shipOrder(orderId, companyCode, companyName, trackingNo);
        return Result.success("发货成功", result);
    }

    /**
     * 取消订单
     */
    @PutMapping("/{orderId}/cancel")
    @RequireLogin
    @Operation(summary = "取消订单", description = "管理员取消订单")
    public Result<Boolean> cancelOrder(
            @Parameter(description = "订单ID")
            @PathVariable Long orderId) {
        log.info("管理员取消订单, orderId={}", orderId);
        
        Boolean result = orderService.adminCancelOrder(orderId);
        return Result.success("订单已取消", result);
    }
}
