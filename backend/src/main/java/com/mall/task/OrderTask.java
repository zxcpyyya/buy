package com.mall.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 订单定时任务
 * 
 * 功能说明：
 * - 定时检查30分钟未支付的订单并自动关闭
 * - 恢复已关闭订单的库存
 * 
 * 
 * 1. 使用ScheduledExecutorService（通过@EnableScheduling实现）
 * 2. 使用@Transactional保证事务一致性
 * 3. 完善的日志记录
 * 4. 并发安全处理
 * 
 * @author xiu
 * @date 2024/01/01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTask {
    
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
     * 订单超时时间（分钟）
     */
    private static final int ORDER_TIMEOUT_MINUTES = 30;
    
    /**
     * 定时检查未支付订单并自动关闭
     * 
     * 执行频率：每5分钟执行一次
     * 
     * 
     * 1. 使用占位符日志打印
     * 2. 事务保证数据一致性
     * 3. 异常处理完善
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void closeUnpaidOrders() {
        log.info("开始执行未支付订单关闭任务");
        
        try {
            // 计算超时时间点
            LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(ORDER_TIMEOUT_MINUTES);
            
            // 查询超时未支付的订单
            // 订单状态：1-待支付
            LambdaQueryWrapper<OrderInfoDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderInfoDO::getOrderStatus, 1) // 待支付状态
                .lt(OrderInfoDO::getCreateTime, timeoutTime) // 创建时间早于超时时间
                .orderByAsc(OrderInfoDO::getCreateTime); // 按创建时间升序，先关闭最早的订单
            
            List<OrderInfoDO> unpaidOrders = orderInfoMapper.selectList(wrapper);
            
            // 使用isEmpty()而非size()==0
            if (CollectionUtils.isEmpty(unpaidOrders)) {
                log.debug("没有需要关闭的未支付订单");
                return;
            }
            
            log.info("发现{}个未支付订单需要关闭", unpaidOrders.size());
            
            int successCount = 0;
            int failCount = 0;
            
            for (OrderInfoDO order : unpaidOrders) {
                try {
                    // 关闭订单
                    this.closeOrder(order);
                    successCount++;
                    
                    log.info("订单关闭成功, orderId={}, orderNo={}, createTime={}", 
                        order.getId(), order.getOrderNo(), order.getCreateTime());
                } catch (Exception e) {
                    failCount++;
                    log.error("订单关闭失败, orderId={}, orderNo={}, errorMessage={}", 
                        order.getId(), order.getOrderNo(), e.getMessage(), e);
                }
            }
            
            log.info("未支付订单关闭任务执行完成, 成功={}, 失败={}", successCount, failCount);
            
        } catch (Exception e) {
            log.error("未支付订单关闭任务执行异常, errorMessage={}", e.getMessage(), e);
            // 异常上抛，触发事务回滚
            throw e;
        }
    }
    
    /**
     * 关闭订单并恢复库存
     * 
     * @param order 订单信息
     */
    private void closeOrder(OrderInfoDO order) {
        // 1. 获取订单商品项
        List<OrderItemDO> orderItems = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemDO>()
                .eq(OrderItemDO::getOrderId, order.getId())
        );
        
        // 2. 恢复库存和销量
        for (OrderItemDO item : orderItems) {
            // 恢复库存
            productService.updateStock(item.getProductId(), item.getQuantity());
            
            // 减少销量（库存使用Integer）
            var product = productMapper.selectById(item.getProductId());
            if (Objects.nonNull(product)) {
                LambdaUpdateWrapper<com.mall.entity.ProductDO> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(com.mall.entity.ProductDO::getId, product.getId())
                    .set(com.mall.entity.ProductDO::getSales, 
                        Math.max(0, product.getSales() - item.getQuantity()));
                productMapper.update(null, wrapper);
            }
            
            log.debug("恢复商品库存, productId={}, quantity={}", item.getProductId(), item.getQuantity());
        }
        
        // 3. 更新订单状态为已取消
        LambdaUpdateWrapper<OrderInfoDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderInfoDO::getId, order.getId())
            .eq(OrderInfoDO::getOrderStatus, 1) // 乐观锁：确保订单状态未变化
            .set(OrderInfoDO::getOrderStatus, 5); // 已取消状态
        
        int updatedRows = orderInfoMapper.update(null, updateWrapper);
        
        if (updatedRows == 0) {
            log.warn("订单状态已变化，跳过关闭, orderId={}", order.getId());
            return;
        }
        
        log.info("订单已关闭, orderId={}, orderNo={}", order.getId(), order.getOrderNo());
    }
    
    /**
     * 每日凌晨清理过期订单数据（可选扩展功能）
     * 
     * 执行频率：每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredOrders() {
        log.info("开始执行过期订单清理任务");
        
        // 此功能可根据业务需求扩展
        // 例如：清理超过3个月的已取消订单
        
        log.info("过期订单清理任务执行完成");
    }
}
