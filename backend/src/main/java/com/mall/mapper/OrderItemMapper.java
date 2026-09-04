package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.OrderItemDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单商品项Mapper接口
 * 
 * @author xiu
 * @date 2024/01/01
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemDO> {
}
