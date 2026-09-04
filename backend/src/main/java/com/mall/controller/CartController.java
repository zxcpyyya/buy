package com.mall.controller;

import com.mall.common.result.Result;
import com.mall.context.UserContext;
import com.mall.dto.CartItemDTO;
import com.mall.service.CartService;
import com.mall.vo.CartItemVO;
import com.mall.vo.CartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车Controller
 * 
 * 
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "购物车管理", description = "购物车操作接口")
public class CartController {
    
    /**
     * 购物车服务
     */
    private final CartService cartService;
    
    /**
     * 获取购物车
     * 
     * GET /api/cart
     */
    @GetMapping
    @Operation(summary = "获取购物车", description = "获取当前用户购物车")
    public Result<CartVO> getCart() {
        Long userId = UserContext.getUserId();
        
        log.debug("获取购物车请求, userId={}", userId);
        
        CartVO cartVO = cartService.getCart(userId);
        
        return Result.success(cartVO);
    }
    
    /**
     * 添加商品到购物车
     * 
     * POST /api/cart
     */
    @PostMapping
    @Operation(summary = "添加购物车", description = "添加商品到购物车")
    public Result<Long> addToCart(@RequestBody CartItemDTO cartItemDTO) {
        Long userId = UserContext.getUserId();
        
        log.info("添加购物车请求, userId={}, productId={}, quantity={}", 
            userId, cartItemDTO.getProductId(), cartItemDTO.getQuantity());
        
        Long cartId = cartService.addToCart(cartItemDTO, userId);
        
        return Result.success("添加成功", cartId);
    }
    
    /**
     * 更新购物车商品数量
     * 
     * PUT /api/cart/item/{id}
     */
    @PutMapping("/item/{id}")
    @Operation(summary = "更新数量", description = "更新购物车商品数量")
    public Result<Boolean> updateQuantity(
            @Parameter(description = "购物车商品ID")
            @PathVariable Long id,
            @Parameter(description = "数量")
            @RequestParam Integer quantity) {
        Long userId = UserContext.getUserId();
        
        log.info("更新购物车数量请求, userId={}, cartItemId={}, quantity={}", 
            userId, id, quantity);
        
        Boolean result = cartService.updateQuantity(id, quantity, userId);
        
        return Result.success(result);
    }
    
    /**
     * 从购物车移除商品
     * 
     * DELETE /api/cart/item/{id}
     */
    @DeleteMapping("/item/{id}")
    @Operation(summary = "移除商品", description = "从购物车移除商品")
    public Result<Boolean> removeFromCart(
            @Parameter(description = "购物车商品ID")
            @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        
        log.info("移除购物车商品请求, userId={}, cartItemId={}", userId, id);
        
        Boolean result = cartService.removeFromCart(id, userId);
        
        return Result.success(result);
    }
    
    /**
     * 清空购物车
     * 
     * DELETE /api/cart
     */
    @DeleteMapping
    @Operation(summary = "清空购物车", description = "清空购物车所有商品")
    public Result<Boolean> clearCart() {
        Long userId = UserContext.getUserId();
        
        log.info("清空购物车请求, userId={}", userId);
        
        Boolean result = cartService.clearCart(userId);
        
        return Result.success(result);
    }
    
    /**
     * 获取购物车商品数量
     * 
     * GET /api/cart/count
     */
    @GetMapping("/count")
    @Operation(summary = "购物车数量", description = "获取购物车商品总数量")
    public Result<Integer> getCartItemCount() {
        Long userId = UserContext.getUserId();
        
        log.debug("获取购物车数量请求, userId={}", userId);
        
        Integer count = cartService.getCartItemCount(userId);
        
        return Result.success(count);
    }
}
