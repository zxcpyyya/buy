package com.mall.service;

import com.mall.dto.AddressDTO;
import com.mall.vo.AddressVO;

import java.util.List;

/**
 * 收货地址Service接口
 * 
 * @author xiu
 * @date 2024/01/01
 */
public interface AddressService {
    
    /**
     * 获取用户收货地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<AddressVO> getUserAddresses(Long userId);
    
    /**
     * 获取收货地址详情
     *
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 地址详情
     */
    AddressVO getAddressDetail(Long addressId, Long userId);
    
    /**
     * 创建收货地址
     *
     * @param addressDTO 地址信息
     * @param userId 用户ID
     * @return 地址ID
     */
    Long createAddress(AddressDTO addressDTO, Long userId);
    
    /**
     * 更新收货地址
     *
     * @param addressId 地址ID
     * @param addressDTO 地址信息
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean updateAddress(Long addressId, AddressDTO addressDTO, Long userId);
    
    /**
     * 删除收货地址
     *
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteAddress(Long addressId, Long userId);
    
    /**
     * 设置默认收货地址
     *
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean setDefaultAddress(Long addressId, Long userId);
}
