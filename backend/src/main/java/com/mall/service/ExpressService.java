package com.mall.service;

import com.mall.dto.ExpressCreateDTO;
import com.mall.vo.ExpressVO;

/**
 * 物流服务接口
 *
 * @author xiu
 */
public interface ExpressService {

    /**
     * 获取订单物流信息
     *
     * @param orderId 订单ID
     * @param userId  用户ID（用于权限校验）
     * @return 物流信息
     */
    ExpressVO getOrderExpress(Long orderId, Long userId);

    /**
     * 获取物流详情
     *
     * @param expressId 物流ID
     * @param userId     用户ID
     * @return 物流信息（含轨迹）
     */
    ExpressVO getExpressDetail(Long expressId, Long userId);

    /**
     * 创建物流信息（商家发货时调用）
     *
     * @param dto   物流信息
     * @param userId 操作人ID
     * @return 物流ID
     */
    Long createExpress(ExpressCreateDTO dto, Long userId);

    /**
     * 更新物流状态
     *
     * @param expressId 物流ID
     * @param status    新状态
     * @param message   状态描述
     * @param location  地点
     * @return 是否成功
     */
    Boolean updateExpressStatus(Long expressId, Integer status, String message, String location);

    /**
     * 模拟发货（演示用）
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    Boolean shipOrder(Long orderId);
}
