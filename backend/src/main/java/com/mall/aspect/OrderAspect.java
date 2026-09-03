package com.mall.aspect;

import com.mall.delay.RedisDelayQueue;
import com.mall.entity.OrderInfoDO;
import com.mall.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 订单创建AOP通知
 * 
 * 功能说明：
 * - 当订单创建成功后，自动将订单加入延迟队列
 * - 订单支付后，从延迟队列中移除
 * 
 * 遵循阿里Java开发规范：
 * 1. AOP应谨慎使用，此处用于解耦订单创建和延迟队列逻辑
 * 2. 完善的异常处理
 * 
 * @author mall
 * @date 2024/01/01
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OrderAspect {
    
    /**
     * 延迟队列
     */
    private final RedisDelayQueue delayQueue;
    
    /**
     * 订单创建成功后，加入延迟队列
     *
     * @param orderVO 订单VO
     */
    @AfterReturning(returning = "orderVO", pointcut = "execution(* com.mall.service.OrderService.createOrder(..))")
    public void addToDelayQueue(Object orderVO) {
        try {
            if (orderVO instanceof OrderInfoDO order) {
                delayQueue.addOrder(order.getId(), order.getOrderNo());
                log.debug("AOP通知：订单已加入延迟队列, orderId={}", order.getId());
            }
        } catch (Exception e) {
            // AOP异常不应影响主流程，只记录日志
            log.error("AOP通知失败：订单加入延迟队列异常, errorMessage={}", e.getMessage(), e);
        }
    }
}
