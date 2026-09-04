package com.mall.controller;

import com.mall.common.result.Result;
import com.mall.context.UserContext;
import com.mall.dto.ExpressCreateDTO;
import com.mall.service.ExpressService;
import com.mall.vo.ExpressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 物流Controller
 *
 * @author xiu
 */
@Slf4j
@RestController
@RequestMapping("/api/express")
@RequiredArgsConstructor
@Tag(name = "物流管理", description = "物流查询、发货接口")
public class ExpressController {

    private final ExpressService expressService;

    /**
     * 获取订单物流信息
     *
     * GET /api/express/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "订单物流", description = "获取订单的物流信息")
    public Result<ExpressVO> getOrderExpress(
            @Parameter(description = "订单ID")
            @PathVariable Long orderId) {
        Long userId = UserContext.getUserId();
        ExpressVO express = expressService.getOrderExpress(orderId, userId);
        return Result.success(express);
    }

    /**
     * 获取物流详情（含轨迹）
     *
     * GET /api/express/{expressId}
     */
    @GetMapping("/{expressId}")
    @Operation(summary = "物流详情", description = "获取物流详情和运输轨迹")
    public Result<ExpressVO> getExpressDetail(
            @Parameter(description = "物流ID")
            @PathVariable Long expressId) {
        Long userId = UserContext.getUserId();
        ExpressVO express = expressService.getExpressDetail(expressId, userId);
        return Result.success(express);
    }

    // ========== 管理员接口 ==========

    /**
     * 创建物流（商家发货）
     *
     * POST /api/express
     */
    @PostMapping
    @Operation(summary = "创建物流", description = "商家发货时创建物流信息")
    public Result<Long> createExpress(@Valid @RequestBody ExpressCreateDTO dto) {
        Long userId = UserContext.getUserId();
        Long expressId = expressService.createExpress(dto, userId);
        return Result.success("发货成功", expressId);
    }

    /**
     * 模拟发货（演示用）
     *
     * POST /api/express/ship/{orderId}
     */
    @PostMapping("/ship/{orderId}")
    @Operation(summary = "模拟发货", description = "模拟订单发货（演示用）")
    public Result<Boolean> shipOrder(
            @Parameter(description = "订单ID")
            @PathVariable Long orderId) {
        Boolean result = expressService.shipOrder(orderId);
        return Result.success("发货成功", result);
    }

    /**
     * 更新物流状态
     *
     * PUT /api/express/{expressId}/status
     */
    @PutMapping("/{expressId}/status")
    @Operation(summary = "更新状态", description = "更新物流状态")
    public Result<Boolean> updateStatus(
            @Parameter(description = "物流ID")
            @PathVariable Long expressId,
            @Parameter(description = "新状态")
            @RequestParam Integer status,
            @Parameter(description = "状态描述")
            @RequestParam(required = false) String message,
            @Parameter(description = "地点")
            @RequestParam(required = false) String location) {
        Boolean result = expressService.updateExpressStatus(expressId, status, message, location);
        return Result.success(result);
    }
}
