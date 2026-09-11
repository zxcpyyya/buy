package com.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.dto.AdminOrderQueryDTO;
import com.mall.dto.CreateOrderDTO;
import com.mall.entity.OrderInfoDO;
import com.mall.vo.AdminOrderVO;
import com.mall.vo.OrderStatsVO;
import com.mall.vo.OrderVO;

import java.util.List;

/**
 * 订单Service接口
 * 
 * @author xiu
 * @date 2026/09/03
 */
public interface OrderService {
    
    /**
     * 创建订单
     *
     * @param createOrderDTO 创建订单参数
     * @param userId 用户ID
     * @return 订单VO
     */
    OrderVO createOrder(CreateOrderDTO createOrderDTO, Long userId);
    
    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 订单详情
     */
    OrderVO getOrderDetail(Long orderId, Long userId);
    
    /**
     * 获取用户订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<OrderVO> getUserOrders(Long userId, Integer status, Integer pageNum, Integer pageSize);
    
    /**
     * 获取订单详情（内部使用，包含订单号）
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 订单详情
     */
    OrderInfoDO getOrderById(Long orderId, Long userId);
    
    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean cancelOrder(Long orderId, Long userId);
    
    /**
     * 确认收货
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean confirmReceive(Long orderId, Long userId);
    
    /**
     * 删除订单
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteOrder(Long orderId, Long userId);
    
    /**
     * 支付订单（模拟）
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean payOrder(Long orderId, Long userId);
    
    // ==================== 管理员/商家接口 ====================
    
    /**
     * 后台分页查询订单
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<AdminOrderVO> getAdminOrderPage(AdminOrderQueryDTO queryDTO);
    
    /**
     * 获取订单统计
     *
     * @return 订单统计
     */
    OrderStatsVO getOrderStats();
    
    /**
     * 后台发货
     *
     * @param orderId 订单ID
     * @param companyCode 物流公司编码
     * @param companyName 物流公司名称
     * @param trackingNo 运单号
     * @return 是否成功
     */
    Boolean shipOrder(Long orderId, String companyCode, String companyName, String trackingNo);
    
    /**
     * 后台取消订单
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    Boolean adminCancelOrder(Long orderId);
}
