package com.mall.service;

import com.mall.vo.PointsRecordVO;

import java.util.List;

/**
 * 积分服务接口
 *
 * @author mall
 */
public interface PointsService {

    /**
     * 获取用户当前积分余额
     *
     * @param userId 用户ID
     * @return 积分余额
     */
    Integer getUserPoints(Long userId);

    /**
     * 获取积分记录列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 积分记录列表
     */
    List<PointsRecordVO> getPointsRecords(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 增加积分
     *
     * @param userId      用户ID
     * @param points      积分数量
     * @param type        类型：1-订单获取，3-活动赠送
     * @param orderId     关联订单ID（可选）
     * @param description 描述
     * @return 是否成功
     */
    Boolean addPoints(Long userId, Integer points, Integer type, Long orderId, String description);

    /**
     * 使用积分
     *
     * @param userId      用户ID
     * @param points      积分数量
     * @param orderId     关联订单ID
     * @param description 描述
     * @return 是否成功
     */
    Boolean usePoints(Long userId, Integer points, Long orderId, String description);

    /**
     * 扣除积分（过期等）
     *
     * @param userId      用户ID
     * @param points      积分数量
     * @param description 描述
     * @return 是否成功
     */
    Boolean deductPoints(Long userId, Integer points, String description);

    /**
     * 获取用户总积分（累计获取）
     *
     * @param userId 用户ID
     * @return 总积分
     */
    Integer getTotalPoints(Long userId);
}
