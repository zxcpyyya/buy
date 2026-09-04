package com.mall.service;

import com.mall.dto.CartItemDTO;
import com.mall.vo.CartItemVO;
import com.mall.vo.CartVO;

import java.util.List;

/**
 * 购物车Service接口
 * 
 * @author xiu
 * @date 2026/09/03
 */
public interface CartService {
    
    /**
     * 获取用户购物车
     *
     * @param userId 用户ID
     * @return 购物车信息
     */
    CartVO getCart(Long userId);
    
    /**
     * 添加商品到购物车
     *
     * @param cartItemDTO 购物车商品信息
     * @param userId 用户ID
     * @return 购物车ID
     */
    Long addToCart(CartItemDTO cartItemDTO, Long userId);
    
    /**
     * 更新购物车商品数量
     *
     * @param cartItemId 购物车商品ID
     * @param quantity 数量
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean updateQuantity(Long cartItemId, Integer quantity, Long userId);
    
    /**
     * 从购物车移除商品
     *
     * @param cartItemId 购物车商品ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean removeFromCart(Long cartItemId, Long userId);
    
    /**
     * 清空购物车
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean clearCart(Long userId);
    
    /**
     * 获取购物车商品数量
     *
     * @param userId 用户ID
     * @return 商品数量
     */
    Integer getCartItemCount(Long userId);
}
