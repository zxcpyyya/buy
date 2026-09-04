package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.CartItemDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车商品项Mapper接口
 * 
 * @author xiu
 * @date 2024/01/01
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItemDO> {
}
