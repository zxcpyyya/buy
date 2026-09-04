package com.mall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.dto.CartItemDTO;
import com.mall.entity.CartDO;
import com.mall.entity.CartItemDO;
import com.mall.entity.ProductDO;
import com.mall.mapper.CartItemMapper;
import com.mall.mapper.CartMapper;
import com.mall.mapper.ProductMapper;
import com.mall.service.CartService;
import com.mall.vo.CartItemVO;
import com.mall.vo.CartVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 购物车Service实现类
 * 
 * @author xiu
 * @date 2024/01/01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    
    /**
     * 获取用户购物车
     */
    @Override
    public CartVO getCart(Long userId) {
        // 获取或创建购物车
        CartDO cart = this.getOrCreateCart(userId);
        
        // 获取购物车商品
        LambdaQueryWrapper<CartItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItemDO::getCartId, cart.getId());
        
        List<CartItemDO> cartItems = cartItemMapper.selectList(wrapper);
        
        // 转换为VO并计算小计
        List<CartItemVO> items = cartItems.stream()
            .map(this::convertToItemVO)
            .collect(Collectors.toList());
        
        // 计算总价和总数量
        BigDecimal totalPrice = items.stream()
            .map(item -> item.getSubtotal())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalCount = items.stream()
            .mapToInt(CartItemVO::getQuantity)
            .sum();
        
        // 构建购物车VO
        CartVO cartVO = new CartVO();
        cartVO.setId(cart.getId());
        cartVO.setItems(items);
        cartVO.setTotalPrice(totalPrice);
        cartVO.setTotalCount(totalCount);
        cartVO.setAllSelected(!items.isEmpty() && items.stream().allMatch(CartItemVO::getSelected));
        
        return cartVO;
    }
    
    /**
     * 添加商品到购物车
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addToCart(CartItemDTO cartItemDTO, Long userId) {
        // 校验商品是否存在
        ProductDO product = productMapper.selectById(cartItemDTO.getProductId());
        if (Objects.isNull(product)) {
            throw new BusinessException("A0401", "商品不存在");
        }
        
        if (product.getStatus() == 0) {
            throw new BusinessException("A0401", "商品已下架");
        }
        
        if (product.getStock() < cartItemDTO.getQuantity()) {
            throw new BusinessException("A0401", "库存不足");
        }
        
        // 获取或创建购物车
        CartDO cart = this.getOrCreateCart(userId);
        
        // 检查是否已存在该商品
        LambdaQueryWrapper<CartItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItemDO::getCartId, cart.getId())
            .eq(CartItemDO::getProductId, cartItemDTO.getProductId());
        
        CartItemDO existItem = cartItemMapper.selectOne(wrapper);
        
        if (Objects.nonNull(existItem)) {
            // 更新数量
            int newQuantity = existItem.getQuantity() + cartItemDTO.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new BusinessException("A0401", "库存不足");
            }
            
            existItem.setQuantity(newQuantity);
            cartItemMapper.updateById(existItem);
            
            log.info("更新购物车商品数量, cartItemId={}, quantity={}", existItem.getId(), newQuantity);
            return cart.getId();
        } else {
            // 添加新商品
            CartItemDO cartItemDO = new CartItemDO();
            cartItemDO.setCartId(cart.getId());
            cartItemDO.setProductId(cartItemDTO.getProductId());
            cartItemDO.setQuantity(cartItemDTO.getQuantity());
            cartItemDO.setPrice(product.getPrice());
            
            cartItemMapper.insert(cartItemDO);
            
            log.info("添加商品到购物车, cartId={}, productId={}, quantity={}", 
                cart.getId(), cartItemDTO.getProductId(), cartItemDTO.getQuantity());
            return cart.getId();
        }
    }
    
    /**
     * 更新购物车商品数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateQuantity(Long cartItemId, Integer quantity, Long userId) {
        CartItemDO cartItem = cartItemMapper.selectById(cartItemId);
        if (Objects.isNull(cartItem)) {
            throw new BusinessException("A0401", "购物车商品不存在");
        }
        
        // 权限校验
        CartDO cart = cartMapper.selectById(cartItem.getCartId());
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException("A0301", "无权限操作该购物车");
        }
        
        if (quantity <= 0) {
            throw new BusinessException("A0402", "数量必须大于0");
        }
        
        // 校验库存
        ProductDO product = productMapper.selectById(cartItem.getProductId());
        if (product.getStock() < quantity) {
            throw new BusinessException("A0401", "库存不足");
        }
        
        cartItem.setQuantity(quantity);
        return cartItemMapper.updateById(cartItem) > 0;
    }
    
    /**
     * 从购物车移除商品
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean removeFromCart(Long cartItemId, Long userId) {
        CartItemDO cartItem = cartItemMapper.selectById(cartItemId);
        if (Objects.isNull(cartItem)) {
            throw new BusinessException("A0401", "购物车商品不存在");
        }
        
        // 权限校验
        CartDO cart = cartMapper.selectById(cartItem.getCartId());
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException("A0301", "无权限操作该购物车");
        }
        
        return cartItemMapper.deleteById(cartItemId) > 0;
    }
    
    /**
     * 清空购物车
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean clearCart(Long userId) {
        CartDO cart = this.getOrCreateCart(userId);
        
        LambdaQueryWrapper<CartItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItemDO::getCartId, cart.getId());
        
        return cartItemMapper.delete(wrapper) >= 0;
    }
    
    /**
     * 获取购物车商品数量
     */
    @Override
    public Integer getCartItemCount(Long userId) {
        CartDO cart = this.getOrCreateCart(userId);
        
        LambdaQueryWrapper<CartItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItemDO::getCartId, cart.getId());
        
        List<CartItemDO> items = cartItemMapper.selectList(wrapper);
        
        // 使用isEmpty()而非size()==0
        if (CollectionUtils.isEmpty(items)) {
            return 0;
        }
        
        return items.stream()
            .mapToInt(CartItemDO::getQuantity)
            .sum();
    }
    
    /**
     * 获取或创建购物车
     */
    private CartDO getOrCreateCart(Long userId) {
        LambdaQueryWrapper<CartDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartDO::getUserId, userId);
        
        CartDO cart = cartMapper.selectOne(wrapper);
        
        if (Objects.isNull(cart)) {
            cart = new CartDO();
            cart.setUserId(userId);
            cart.setTotalPrice(BigDecimal.ZERO);
            cart.setTotalCount(0);
            cartMapper.insert(cart);
            
            log.info("创建新购物车, cartId={}, userId={}", cart.getId(), userId);
        }
        
        return cart;
    }
    
    /**
     * 转换为购物车商品项VO
     */
    private CartItemVO convertToItemVO(CartItemDO cartItemDO) {
        CartItemVO cartItemVO = new CartItemVO();
        cartItemVO.setId(cartItemDO.getId());
        cartItemVO.setProductId(cartItemDO.getProductId());
        cartItemVO.setPrice(cartItemDO.getPrice());
        cartItemVO.setQuantity(cartItemDO.getQuantity());
        cartItemVO.setSubtotal(cartItemDO.getPrice().multiply(BigDecimal.valueOf(cartItemDO.getQuantity())));
        cartItemVO.setSelected(true);
        
        // 获取商品信息
        ProductDO product = productMapper.selectById(cartItemDO.getProductId());
        if (Objects.nonNull(product)) {
            cartItemVO.setProductName(product.getName());
            cartItemVO.setImage(product.getImage());
            cartItemVO.setStock(product.getStock());
        }
        
        return cartItemVO;
    }
}
