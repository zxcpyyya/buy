package com.mall.service;

/**
 * 延迟队列服务接口
 * 
 * 用于处理订单超时等延迟任务，避免轮询数据库
 * 
 * @author xiu
 * @date 2026/09/03
 */
public interface DelayQueueService {
    
    /**
     * 添加延迟任务
     *
     * @param orderId 订单ID
     * @param delayMs 延迟时间（毫秒）
     */
    void addDelayTask(Long orderId, long delayMs);
    
    /**
     * 移除延迟任务
     *
     * @param orderId 订单ID
     */
    void removeDelayTask(Long orderId);
    
    /**
     * 获取到期任务数量
     *
     * @return 到期任务数量
     */
    Long getExpiredTaskCount();
    
    /**
     * 消费到期任务
     * 返回到期订单ID列表
     *
     * @param limit 每次最多获取数量
     * @return 到期订单ID列表
     */
    java.util.List<Long> pollExpiredTasks(int limit);
}
