package com.mall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.dto.AddressDTO;
import com.mall.entity.AddressDO;
import com.mall.mapper.AddressMapper;
import com.mall.service.AddressService;
import com.mall.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 收货地址Service实现类
 * 
 * @author mall
 * @date 2024/01/01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    
    private final AddressMapper addressMapper;
    
    /**
     * 获取用户收货地址列表
     */
    @Override
    public List<AddressVO> getUserAddresses(Long userId) {
        LambdaQueryWrapper<AddressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressDO::getUserId, userId)
            .orderByDesc(AddressDO::getIsDefault)
            .orderByDesc(AddressDO::getCreateTime);
        
        List<AddressDO> addresses = addressMapper.selectList(wrapper);
        
        return addresses.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取收货地址详情
     */
    @Override
    public AddressVO getAddressDetail(Long addressId, Long userId) {
        AddressDO addressDO = addressMapper.selectById(addressId);
        if (Objects.isNull(addressDO)) {
            throw new BusinessException("A0401", "收货地址不存在");
        }
        
        // 权限校验（阿里规范：水平权限校验）
        if (!addressDO.getUserId().equals(userId)) {
            throw new BusinessException("A0301", "无权限访问该地址");
        }
        
        return convertToVO(addressDO);
    }
    
    /**
     * 创建收货地址
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createAddress(AddressDTO addressDTO, Long userId) {
        // 如果设置为默认地址，先取消其他默认地址
        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault() == 1) {
            this.cancelDefaultAddress(userId);
        }
        
        AddressDO addressDO = new AddressDO();
        addressDO.setUserId(userId);
        addressDO.setConsignee(addressDTO.getConsignee());
        addressDO.setPhone(addressDTO.getPhone());
        addressDO.setProvince(addressDTO.getProvince());
        addressDO.setCity(addressDTO.getCity());
        addressDO.setDistrict(addressDTO.getDistrict());
        addressDO.setDetailAddress(addressDTO.getDetailAddress());
        addressDO.setIsDefault(addressDTO.getIsDefault() != null ? addressDTO.getIsDefault() : 0);
        addressDO.setLabel(addressDTO.getLabel());
        
        addressMapper.insert(addressDO);
        
        log.info("创建收货地址成功, addressId={}, userId={}", addressDO.getId(), userId);
        
        return addressDO.getId();
    }
    
    /**
     * 更新收货地址
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateAddress(Long addressId, AddressDTO addressDTO, Long userId) {
        AddressDO addressDO = addressMapper.selectById(addressId);
        if (Objects.isNull(addressDO)) {
            throw new BusinessException("A0401", "收货地址不存在");
        }
        
        // 权限校验
        if (!addressDO.getUserId().equals(userId)) {
            throw new BusinessException("A0301", "无权限修改该地址");
        }
        
        // 如果设置为默认地址，先取消其他默认地址
        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault() == 1) {
            this.cancelDefaultAddress(userId);
        }
        
        // 更新字段
        addressDO.setConsignee(addressDTO.getConsignee());
        addressDO.setPhone(addressDTO.getPhone());
        addressDO.setProvince(addressDTO.getProvince());
        addressDO.setCity(addressDTO.getCity());
        addressDO.setDistrict(addressDTO.getDistrict());
        addressDO.setDetailAddress(addressDTO.getDetailAddress());
        addressDO.setIsDefault(addressDTO.getIsDefault() != null ? addressDTO.getIsDefault() : 0);
        addressDO.setLabel(addressDTO.getLabel());
        
        return addressMapper.updateById(addressDO) > 0;
    }
    
    /**
     * 删除收货地址
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean deleteAddress(Long addressId, Long userId) {
        AddressDO addressDO = addressMapper.selectById(addressId);
        if (Objects.isNull(addressDO)) {
            throw new BusinessException("A0401", "收货地址不存在");
        }
        
        // 权限校验
        if (!addressDO.getUserId().equals(userId)) {
            throw new BusinessException("A0301", "无权限删除该地址");
        }
        
        return addressMapper.deleteById(addressId) > 0;
    }
    
    /**
     * 设置默认收货地址
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean setDefaultAddress(Long addressId, Long userId) {
        AddressDO addressDO = addressMapper.selectById(addressId);
        if (Objects.isNull(addressDO)) {
            throw new BusinessException("A0401", "收货地址不存在");
        }
        
        // 权限校验
        if (!addressDO.getUserId().equals(userId)) {
            throw new BusinessException("A0301", "无权限操作该地址");
        }
        
        // 取消其他默认地址
        this.cancelDefaultAddress(userId);
        
        // 设置当前地址为默认
        LambdaUpdateWrapper<AddressDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressDO::getId, addressId)
            .set(AddressDO::getIsDefault, 1);
        
        return addressMapper.update(null, wrapper) > 0;
    }
    
    /**
     * 取消所有默认地址
     */
    private void cancelDefaultAddress(Long userId) {
        LambdaUpdateWrapper<AddressDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressDO::getUserId, userId)
            .eq(AddressDO::getIsDefault, 1)
            .set(AddressDO::getIsDefault, 0);
        addressMapper.update(null, wrapper);
    }
    
    /**
     * 转换为VO对象
     */
    private AddressVO convertToVO(AddressDO addressDO) {
        AddressVO addressVO = BeanUtil.copyProperties(addressDO, AddressVO.class);
        
        // 拼接完整地址
        String fullAddress = String.join("", 
            addressDO.getProvince(), 
            addressDO.getCity(), 
            addressDO.getDistrict(), 
            addressDO.getDetailAddress());
        addressVO.setFullAddress(fullAddress);
        
        return addressVO;
    }
}
