package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.AddressDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货地址Mapper接口
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Mapper
public interface AddressMapper extends BaseMapper<AddressDO> {
}
