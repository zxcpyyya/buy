package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.OrderInfoDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper接口
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfoDO> {
}
