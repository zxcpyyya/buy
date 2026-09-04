package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.CartItemDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车商品项Mapper接口
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItemDO> {
}
