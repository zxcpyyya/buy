package com.mall.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.delay.RedisDelayQueue;
import com.mall.entity.OrderInfoDO;
import com.mall.entity.OrderItemDO;
import com.mall.mapper.OrderInfoMapper;
import com.mall.mapper.OrderItemMapper;
import com.mall.mapper.ProductMapper;
import com.mall.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Objects;
import java.util.Set;

/**
 * 订单超时处理任务（基于Redis延迟队列）
 * 
 * 相比定时轮询方案的优势：
 * 1. 无需全表扫描数据库 - 只处理到期的订单
 * 2. 精准定时 - 订单超时时间一到立即处理，无延迟
 * 3. 低资源消耗 - 无订单超时时不消耗数据库资源
 * 4. 支持分布式 - 多实例共享同一个Redis队列
 * 5. 高性能 - 基于Redis ZSet，时间复杂度O(logN)
 * 
 * 
 * 1. 使用ScheduledExecutorService
 * 2. @Transactional保证事务一致性
 * 3. 完善的日志记录
 * 4. 异常处理完善
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDelayTask {
    
    /**
     * 订单Mapper
     */
    private final OrderInfoMapper orderInfoMapper;
    
    /**
     * 订单商品项Mapper
     */
    private final OrderItemMapper orderItemMapper;
    
    /**
     * 商品Mapper
     */
    private final ProductMapper productMapper;
    
    /**
     * 商品服务
     */
    private final ProductService productService;
    
    /**
     * Redis延迟队列
     */
    private final RedisDelayQueue delayQueue;
    
    /**
     * 处理到期的未支付订单
     * 
     * 执行频率：每秒执行一次，快速响应
     * 
     * 使用说明：
     * - 相比5分钟轮询，现在每秒检查并处理到期订单
     * - 订单一旦到期，立即处理，延迟从5分钟降至1秒内
     * - 无需轮询数据库，只处理Redis中到期的订单
     */
    @Scheduled(fixedRate = 1000)
    @Transactional(rollbackFor = Exception.class)
    public void processExpiredOrders() {
        try {
            // 1. 从Redis延迟队列获取到期的订单
            Set<Object> expiredOrderIds = delayQueue.getExpiredOrders();
            
            // 2. 使用isEmpty()而非size()==0
            if (CollectionUtils.isEmpty(expiredOrderIds)) {
                return;
            }
            
            log.info("开始处理{}个到期订单", expiredOrderIds.size());
            
            int successCount = 0;
            int failCount = 0;
            
            // 3. 遍历处理每个到期订单
            for (Object orderIdObj : expiredOrderIds) {
                Long orderId = Long.parseLong(orderIdObj.toString());
                
                try {
                    this.processOrder(orderId);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("处理到期订单失败, orderId={}, errorMessage={}", 
                        orderId, e.getMessage(), e);
                }
            }
            
            if (successCount > 0 || failCount > 0) {
                log.info("到期订单处理完成, 成功={}, 失败={}", successCount, failCount);
            }
            
        } catch (Exception e) {
            log.error("处理到期订单任务异常, errorMessage={}", e.getMessage(), e);
            // 异常不上抛，避免阻塞后续执行
        }
    }
    
    /**
     * 处理单个到期订单
     *
     * @param orderId 订单ID
     */
    private void processOrder(Long orderId) {
        // 1. 查询订单信息
        OrderInfoDO order = orderInfoMapper.selectById(orderId);
        if (Objects.isNull(order)) {
            log.warn("订单不存在, orderId={}", orderId);
            delayQueue.removeProcessedOrder(orderId);
            return;
        }
        
        // 2. 检查订单状态（只有待支付状态才处理）
        if (order.getOrderStatus() != 1) {
            log.debug("订单状态已变化，跳过处理, orderId={}, orderStatus={}", 
                orderId, order.getOrderStatus());
            delayQueue.removeProcessedOrder(orderId);
            return;
        }
        
        // 3. 获取订单商品项
        var orderItems = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemDO>()
                .eq(OrderItemDO::getOrderId, orderId)
        );
        
        // 4. 恢复库存和销量
        for (OrderItemDO item : orderItems) {
            // 恢复库存
            productService.updateStock(item.getProductId(), item.getQuantity());
            
            // 减少销量
            var product = productMapper.selectById(item.getProductId());
            if (Objects.nonNull(product)) {
                LambdaUpdateWrapper<com.mall.entity.ProductDO> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(com.mall.entity.ProductDO::getId, product.getId())
                    .set(com.mall.entity.ProductDO::getSales, 
                        Math.max(0, product.getSales() - item.getQuantity()));
                productMapper.update(null, wrapper);
            }
            
            log.debug("恢复商品库存, productId={}, quantity={}", 
                item.getProductId(), item.getQuantity());
        }
        
        // 5. 更新订单状态为已取消
        LambdaUpdateWrapper<OrderInfoDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderInfoDO::getId, orderId)
            .eq(OrderInfoDO::getOrderStatus, 1) // 乐观锁
            .set(OrderInfoDO::getOrderStatus, 5); // 已取消
        
        int updatedRows = orderInfoMapper.update(null, updateWrapper);
        
        if (updatedRows == 0) {
            log.warn("订单状态已变化（并发），跳过更新, orderId={}", orderId);
            delayQueue.removeProcessedOrder(orderId);
            return;
        }
        
        // 6. 从延迟队列中移除（放在最后，确保处理成功）
        delayQueue.removeProcessedOrder(orderId);
        
        log.info("订单已自动关闭, orderId={}, orderNo={}, userId={}", 
            order.getId(), order.getOrderNo(), order.getUserId());
    }
    
    /**
     * 打印队列状态（监控用）
     * 每分钟打印一次
     */
    @Scheduled(fixedRate = 60000)
    public void printQueueStatus() {
        try {
            Long queueSize = delayQueue.getQueueSize();
            if (queueSize > 0) {
                log.info("延迟队列状态, 待处理订单数={}", queueSize);
            }
        } catch (Exception e) {
            log.error("打印队列状态失败, errorMessage={}", e.getMessage(), e);
        }
    }
}
