package com.mall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BusinessException;
import com.mall.dto.CreateOrderDTO;
import com.mall.entity.*;
import com.mall.mapper.*;
import com.mall.service.OrderService;
import com.mall.service.ProductService;
import com.mall.sharding.ShardingHintUtils;
import com.mall.sharding.ShardingKeyUtils;
import com.mall.vo.OrderItemVO;
import com.mall.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 订单Service实现类
 * 
 * @author mall
 * @date 2024/01/01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final ProductService productService;
    
    /**
     * 创建订单
     * 
     * 遵循阿里Java开发规范：
     * 1. 使用@Transactional保证事务一致性
     * 2. 防止并发超卖问题
     * 3. 完善的异常处理
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderVO createOrder(CreateOrderDTO createOrderDTO, Long userId) {
        // 1. 校验收货地址（阿里规范：NPE防护）
        AddressDO address = addressMapper.selectById(createOrderDTO.getAddressId());
        if (Objects.isNull(address) || !address.getUserId().equals(userId)) {
            throw new BusinessException("A0401", "收货地址不存在或无权限");
        }
        
        // 2. 获取购物车商品（阿里规范：集合处理使用isEmpty()判断）
        List<CartItemDO> cartItems = cartItemMapper.selectList(
            new LambdaQueryWrapper<CartItemDO>()
                .eq(CartItemDO::getCartId, createOrderDTO.getCartId())
        );
        
        if (CollectionUtils.isEmpty(cartItems)) {
            throw new BusinessException("A0401", "购物车为空");
        }
        
        // 3. 计算订单金额（阿里规范：货币金额使用BigDecimal）
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItemDO item : cartItems) {
            BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(subtotal);
        }
        
        // 4. 生成订单号（阿里规范：唯一性保证）
        String orderNo = IdUtil.getSnowflakeNextIdStr();
        
        // 5. 创建订单记录
        OrderInfoDO orderInfoDO = new OrderInfoDO();
        orderInfoDO.setOrderNo(orderNo);
        orderInfoDO.setUserId(userId);
        orderInfoDO.setTotalPrice(totalPrice);
        orderInfoDO.setPayPrice(totalPrice); // 实付金额（简化处理）
        orderInfoDO.setPayType(createOrderDTO.getPayType());
        orderInfoDO.setOrderStatus(1); // 待支付
        orderInfoDO.setDeliveryStatus(0); // 未发货
        orderInfoDO.setReceiverName(address.getConsignee());
        orderInfoDO.setReceiverPhone(address.getPhone());
        orderInfoDO.setReceiverAddress(address.getProvince() + address.getCity() + 
            address.getDistrict() + address.getDetailAddress());
        orderInfoDO.setRemark(createOrderDTO.getRemark());
        
        orderInfoMapper.insert(orderInfoDO);
        
        // 6. 创建订单商品项记录
        List<OrderItemVO> orderItems = new ArrayList<>();
        for (CartItemDO cartItem : cartItems) {
            // 获取商品信息
            ProductDO product = productMapper.selectById(cartItem.getProductId());
            if (Objects.isNull(product)) {
                log.warn("商品不存在, productId={}", cartItem.getProductId());
                continue;
            }
            
            // 扣减库存（阿里规范：并发场景下需要考虑库存问题）
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException("A0401", "商品【" + product.getName() + "】库存不足");
            }
            
            // 更新库存
            productService.updateStock(product.getId(), -cartItem.getQuantity());
            
            // 更新销量
            LambdaUpdateWrapper<ProductDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ProductDO::getId, product.getId())
                .set(ProductDO::getSales, product.getSales() + cartItem.getQuantity());
            productMapper.update(null, wrapper);
            
            // 创建订单项
            OrderItemDO orderItemDO = new OrderItemDO();
            orderItemDO.setOrderId(orderInfoDO.getId());
            orderItemDO.setProductId(product.getId());
            orderItemDO.setProductName(product.getName());
            orderItemDO.setProductImage(product.getImage());
            orderItemDO.setPrice(cartItem.getPrice());
            orderItemDO.setQuantity(cartItem.getQuantity());
            orderItemDO.setTotalPrice(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            
            orderItemMapper.insert(orderItemDO);
            
            // 构建订单项VO
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setId(orderItemDO.getId());
            itemVO.setProductId(product.getId());
            itemVO.setProductName(product.getName());
            itemVO.setProductImage(product.getImage());
            itemVO.setPrice(cartItem.getPrice());
            itemVO.setQuantity(cartItem.getQuantity());
            itemVO.setTotalPrice(orderItemDO.getTotalPrice());
            orderItems.add(itemVO);
        }
        
        // 7. 清空购物车
        cartItemMapper.delete(
            new LambdaQueryWrapper<CartItemDO>()
                .eq(CartItemDO::getCartId, createOrderDTO.getCartId())
        );
        
        log.info("创建订单成功, orderId={}, orderNo={}, userId={}", 
            orderInfoDO.getId(), orderNo, userId);
        
        // 8. 返回订单VO
        return this.convertToVO(orderInfoDO, orderItems);
    }
    
    /**
     * 获取订单详情
     */
    @Override
    public OrderVO getOrderDetail(Long orderId, Long userId) {
        OrderInfoDO orderInfoDO = this.getOrderById(orderId, userId);
        
        // 获取订单商品项
        List<OrderItemDO> orderItems = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemDO>()
                .eq(OrderItemDO::getOrderId, orderId)
        );
        
        List<OrderItemVO> itemVOs = orderItems.stream()
            .map(this::convertToItemVO)
            .collect(Collectors.toList());
        
        return this.convertToVO(orderInfoDO, itemVOs);
    }
    
    /**
     * 获取用户订单列表
     *
     * 性能优化：按 user_id 路由到具体分片，单库单表查询，性能最佳。
     */
    @Override
    public Page<OrderVO> getUserOrders(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<OrderInfoDO> page = new Page<>(pageNum, pageSize);

        // 使用 Hint 强制路由到用户所在的分片
        try (HintManager hintManager = ShardingHintUtils.forceRouteByUserId(userId)) {
            LambdaQueryWrapper<OrderInfoDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderInfoDO::getUserId, userId);

            if (Objects.nonNull(status)) {
                wrapper.eq(OrderInfoDO::getOrderStatus, status);
            }

            wrapper.orderByDesc(OrderInfoDO::getCreateTime);

            Page<OrderInfoDO> result = orderInfoMapper.selectPage(page, wrapper);

            // 转换分页结果
            Page<OrderVO> pageResult = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
            pageResult.setRecords(result.getRecords().stream()
                .map(order -> {
                    List<OrderItemDO> items = orderItemMapper.selectList(
                        new LambdaQueryWrapper<OrderItemDO>()
                            .eq(OrderItemDO::getOrderId, order.getId())
                    );
                    return this.convertToVO(order, items.stream()
                        .map(this::convertToItemVO)
                        .collect(Collectors.toList()));
                })
                .collect(Collectors.toList()));

            return pageResult;
        }
    }
    
    /**
     * 获取订单详情（内部使用）
     *
     * 关键：在 ShardingSphere 分片环境下，订单ID 是雪花算法生成的，
     * 与 user_id 没有直接对应关系。需要使用 Hint 机制轮询所有分片。
     *
     * 性能说明：单次查询会扫描所有分片，但只查询一次（轮询查找）。
     * 如果是高频场景，建议订单号中编码 user_id 以加速查询。
     */
    @Override
    public OrderInfoDO getOrderById(Long orderId, Long userId) {
        OrderInfoDO orderInfoDO = null;

        // 方案1：在所有分片中查找订单
        // 由于雪花算法ID无法直接定位分片，需要遍历所有分片
        for (int dbIndex = 0; dbIndex < ShardingKeyUtils.DB_COUNT; dbIndex++) {
            for (int tableIndex = 0; tableIndex < ShardingKeyUtils.TABLE_COUNT; tableIndex++) {
                try (HintManager hintManager = ShardingHintUtils.forceRoute(dbIndex, tableIndex)) {
                    OrderInfoDO temp = orderInfoMapper.selectById(orderId);
                    if (Objects.nonNull(temp)) {
                        orderInfoDO = temp;
                        break;
                    }
                }
            }
            if (Objects.nonNull(orderInfoDO)) {
                break;
            }
        }

        if (Objects.isNull(orderInfoDO)) {
            throw new BusinessException("A0401", "订单不存在");
        }

        // 权限校验（阿里规范：水平权限校验）
        if (!orderInfoDO.getUserId().equals(userId)) {
            throw new BusinessException("A0301", "无权限访问该订单");
        }

        return orderInfoDO;
    }
    
    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean cancelOrder(Long orderId, Long userId) {
        OrderInfoDO orderInfoDO = this.getOrderById(orderId, userId);
        
        // 只有待支付状态可以取消
        if (orderInfoDO.getOrderStatus() != 1) {
            throw new BusinessException("A0440", "当前状态无法取消订单");
        }
        
        // 恢复库存
        List<OrderItemDO> orderItems = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemDO>()
                .eq(OrderItemDO::getOrderId, orderId)
        );
        
        for (OrderItemDO item : orderItems) {
            // 恢复库存
            productService.updateStock(item.getProductId(), item.getQuantity());
            
            // 减少销量
            ProductDO product = productMapper.selectById(item.getProductId());
            if (Objects.nonNull(product)) {
                LambdaUpdateWrapper<ProductDO> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(ProductDO::getId, product.getId())
                    .set(ProductDO::getSales, Math.max(0, product.getSales() - item.getQuantity()));
                productMapper.update(null, wrapper);
            }
        }
        
        // 更新订单状态
        LambdaUpdateWrapper<OrderInfoDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderInfoDO::getId, orderId)
            .set(OrderInfoDO::getOrderStatus, 5); // 已取消
        
        return orderInfoMapper.update(null, wrapper) > 0;
    }
    
    /**
     * 确认收货
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean confirmReceive(Long orderId, Long userId) {
        OrderInfoDO orderInfoDO = this.getOrderById(orderId, userId);
        
        // 只有已发货状态可以确认收货
        if (orderInfoDO.getOrderStatus() != 3) {
            throw new BusinessException("A0440", "当前状态无法确认收货");
        }
        
        LambdaUpdateWrapper<OrderInfoDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderInfoDO::getId, orderId)
            .set(OrderInfoDO::getOrderStatus, 4) // 已完成
            .set(OrderInfoDO::getReceiveTime, LocalDateTime.now());
        
        return orderInfoMapper.update(null, wrapper) > 0;
    }
    
    /**
     * 删除订单
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean deleteOrder(Long orderId, Long userId) {
        OrderInfoDO orderInfoDO = this.getOrderById(orderId, userId);
        
        // 只有已取消或已完成状态可以删除
        if (orderInfoDO.getOrderStatus() != 4 && orderInfoDO.getOrderStatus() != 5) {
            throw new BusinessException("A0440", "当前状态无法删除订单");
        }
        
        // 删除订单商品项
        orderItemMapper.delete(
            new LambdaQueryWrapper<OrderItemDO>()
                .eq(OrderItemDO::getOrderId, orderId)
        );
        
        // 删除订单（逻辑删除）
        return orderInfoMapper.deleteById(orderId) > 0;
    }
    
    /**
     * 支付订单（模拟）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean payOrder(Long orderId, Long userId) {
        OrderInfoDO orderInfoDO = this.getOrderById(orderId, userId);
        
        // 只有待支付状态可以支付
        if (orderInfoDO.getOrderStatus() != 1) {
            throw new BusinessException("A0440", "当前状态无法支付");
        }
        
        LambdaUpdateWrapper<OrderInfoDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderInfoDO::getId, orderId)
            .set(OrderInfoDO::getOrderStatus, 2) // 已支付
            .set(OrderInfoDO::getPayTime, LocalDateTime.now());
        
        log.info("订单支付成功, orderId={}, orderNo={}, userId={}", 
            orderId, orderInfoDO.getOrderNo(), userId);
        
        return orderInfoMapper.update(null, wrapper) > 0;
    }
    
    /**
     * 转换为订单VO
     */
    private OrderVO convertToVO(OrderInfoDO orderInfoDO, List<OrderItemVO> items) {
        OrderVO orderVO = BeanUtil.copyProperties(orderInfoDO, OrderVO.class);
        orderVO.setItems(items);
        
        // 设置支付方式名称
        if (orderInfoDO.getPayType() != null) {
            orderVO.setPayTypeName(orderInfoDO.getPayType() == 1 ? "微信支付" : "支付宝");
        }
        
        // 设置订单状态名称
        orderVO.setOrderStatusName(this.getOrderStatusName(orderInfoDO.getOrderStatus()));
        
        return orderVO;
    }
    
    /**
     * 转换为订单项VO
     */
    private OrderItemVO convertToItemVO(OrderItemDO orderItemDO) {
        OrderItemVO itemVO = new OrderItemVO();
        itemVO.setId(orderItemDO.getId());
        itemVO.setProductId(orderItemDO.getProductId());
        itemVO.setProductName(orderItemDO.getProductName());
        itemVO.setProductImage(orderItemDO.getProductImage());
        itemVO.setPrice(orderItemDO.getPrice());
        itemVO.setQuantity(orderItemDO.getQuantity());
        itemVO.setTotalPrice(orderItemDO.getTotalPrice());
        return itemVO;
    }
    
    /**
     * 获取订单状态名称
     */
    private String getOrderStatusName(Integer status) {
        return switch (status) {
            case 1 -> "待支付";
            case 2 -> "已支付";
            case 3 -> "已发货";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> "未知状态";
        };
    }
}
