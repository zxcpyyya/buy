package com.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.result.PageResult;
import com.mall.common.result.Result;
import com.mall.context.UserContext;
import com.mall.dto.CreateOrderDTO;
import com.mall.service.OrderService;
import com.mall.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 订单Controller
 * 
 * 遵循RESTful API设计规范和阿里Java开发规约
 * 
 * @author mall
 * @date 2024/01/01
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单创建、查询、状态管理接口")
public class OrderController {
    
    /**
     * 订单服务
     */
    private final OrderService orderService;
    
    /**
     * 创建订单
     * 
     * POST /api/order
     */
    @PostMapping
    @Operation(summary = "创建订单", description = "从购物车创建订单")
    public Result<OrderVO> createOrder(@RequestBody CreateOrderDTO createOrderDTO) {
        Long userId = UserContext.getUserId();
        
        log.info("创建订单请求, userId={}, addressId={}, payType={}", 
            userId, createOrderDTO.getAddressId(), createOrderDTO.getPayType());
        
        OrderVO orderVO = orderService.createOrder(createOrderDTO, userId);
        
        return Result.success("订单创建成功", orderVO);
    }
    
    /**
     * 获取订单详情
     * 
     * GET /api/order/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "订单详情", description = "获取订单详细信息")
    public Result<OrderVO> getOrderDetail(
            @Parameter(description = "订单ID")
            @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        
        log.debug("查询订单详情请求, userId={}, orderId={}", userId, id);
        
        OrderVO orderVO = orderService.getOrderDetail(id, userId);
        
        return Result.success(orderVO);
    }
    
    /**
     * 获取用户订单列表
     * 
     * GET /api/order/list
     */
    @GetMapping("/list")
    @Operation(summary = "订单列表", description = "获取当前用户订单列表")
    public Result<PageResult<OrderVO>> getUserOrders(
            @Parameter(description = "订单状态")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        
        log.debug("查询订单列表请求, userId={}, status={}, pageNum={}, pageSize={}", 
            userId, status, pageNum, pageSize);
        
        Page<OrderVO> page = orderService.getUserOrders(userId, status, pageNum, pageSize);
        
        PageResult<OrderVO> pageResult = PageResult.of(
            page.getRecords(),
            page.getTotal(),
            (int) page.getCurrent(),
            (int) page.getSize()
        );
        
        return Result.success(pageResult);
    }
    
    /**
     * 取消订单
     * 
     * PUT /api/order/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消未支付订单")
    public Result<Boolean> cancelOrder(
            @Parameter(description = "订单ID")
            @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        
        log.info("取消订单请求, userId={}, orderId={}", userId, id);
        
        Boolean result = orderService.cancelOrder(id, userId);
        
        return Result.success("订单已取消", result);
    }
    
    /**
     * 确认收货
     * 
     * PUT /api/order/{id}/confirm
     */
    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认收货", description = "确认已发货订单收货")
    public Result<Boolean> confirmReceive(
            @Parameter(description = "订单ID")
            @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        
        log.info("确认收货请求, userId={}, orderId={}", userId, id);
        
        Boolean result = orderService.confirmReceive(id, userId);
        
        return Result.success("已确认收货", result);
    }
    
    /**
     * 删除订单
     * 
     * DELETE /api/order/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单", description = "删除已完成或已取消订单")
    public Result<Boolean> deleteOrder(
            @Parameter(description = "订单ID")
            @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        
        log.info("删除订单请求, userId={}, orderId={}", userId, id);
        
        Boolean result = orderService.deleteOrder(id, userId);
        
        return Result.success(result);
    }
    
    /**
     * 支付订单（模拟）
     * 
     * POST /api/order/{id}/pay
     */
    @PostMapping("/{id}/pay")
    @Operation(summary = "支付订单", description = "模拟支付订单")
    public Result<Boolean> payOrder(
            @Parameter(description = "订单ID")
            @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        
        log.info("支付订单请求, userId={}, orderId={}", userId, id);
        
        Boolean result = orderService.payOrder(id, userId);
        
        return Result.success("支付成功", result);
    }
}
