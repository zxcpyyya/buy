package com.mall.delay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis延迟队列组件
 * 
 * 基于Redis ZSet实现延迟队列：
 * - Score存储到期时间戳
 * - Value存储订单ID
 * - 定时检查并处理到期的订单
 * 
 * 
 * 1. 使用SLF4J日志框架
 * 2. 完善的异常处理
 * 
 * @author xiu
 * @date 2024/01/01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDelayQueue {
    
    /**
     * Redis模板
     */
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 延迟队列Key
     */
    private static final String DELAY_QUEUE_KEY = "order:delay:unpaid";
    
    /**
     * 订单超时时间（分钟）
     */
    @Value("${order.timeout.minutes:30}")
    private int orderTimeoutMinutes;
    
    /**
     * 将订单加入延迟队列
     *
     * @param orderId 订单ID
     * @param orderNo 订单号
     */
    public void addOrder(Long orderId, String orderNo) {
        try {
            // 计算到期时间戳
            long expireTime = LocalDateTime.now()
                .plusMinutes(orderTimeoutMinutes)
                .toEpochSecond(ZoneOffset.of("+8"));
            
            // 使用ZSet，score为到期时间戳
            ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
            zSetOps.add(DELAY_QUEUE_KEY, orderId.toString(), expireTime);
            
            log.info("订单加入延迟队列成功, orderId={}, orderNo={}, expireTime={}", 
                orderId, orderNo, expireTime);
            
        } catch (Exception e) {
            log.error("订单加入延迟队列失败, orderId={}, orderNo={}, errorMessage={}", 
                orderId, orderNo, e.getMessage(), e);
            throw new RuntimeException("延迟队列操作失败", e);
        }
    }
    
    /**
     * 从延迟队列中移除订单（已支付时调用）
     *
     * @param orderId 订单ID
     */
    public void removeOrder(Long orderId) {
        try {
            ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
            zSetOps.remove(DELAY_QUEUE_KEY, orderId.toString());
            
            log.debug("订单从延迟队列移除, orderId={}", orderId);
            
        } catch (Exception e) {
            log.error("订单从延迟队列移除失败, orderId={}, errorMessage={}", 
                orderId, e.getMessage(), e);
        }
    }
    
    /**
     * 获取已到期的订单ID列表
     *
     * @return 订单ID列表
     */
    public Set<Object> getExpiredOrders() {
        try {
            long now = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
            
            // 查询score <= 当前时间的订单
            ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
            Set<Object> expiredOrders = zSetOps.rangeByScore(DELAY_QUEUE_KEY, 0, now);
            
            if (expiredOrders != null && !expiredOrders.isEmpty()) {
                log.info("发现{}个到期订单", expiredOrders.size());
            }
            
            return expiredOrders;
            
        } catch (Exception e) {
            log.error("获取到期订单失败, errorMessage={}", e.getMessage(), e);
            return Set.of();
        }
    }
    
    /**
     * 删除已处理的订单
     *
     * @param orderId 订单ID
     */
    public void removeProcessedOrder(Long orderId) {
        try {
            ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
            zSetOps.remove(DELAY_QUEUE_KEY, orderId.toString());
            
        } catch (Exception e) {
            log.error("删除已处理订单失败, orderId={}, errorMessage={}", 
                orderId, e.getMessage(), e);
        }
    }
    
    /**
     * 获取队列长度
     *
     * @return 队列长度
     */
    public Long getQueueSize() {
        try {
            ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
            return zSetOps.size(DELAY_QUEUE_KEY);
        } catch (Exception e) {
            log.error("获取队列长度失败, errorMessage={}", e.getMessage(), e);
            return 0L;
        }
    }
    
    /**
     * 检查订单是否在队列中
     *
     * @param orderId 订单ID
     * @return 是否存在
     */
    public Boolean isOrderInQueue(Long orderId) {
        try {
            ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
            Double score = zSetOps.score(DELAY_QUEUE_KEY, orderId.toString());
            return score != null;
        } catch (Exception e) {
            log.error("检查订单队列状态失败, orderId={}, errorMessage={}", 
                orderId, e.getMessage(), e);
            return false;
        }
    }
}
