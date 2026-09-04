package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.CartDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车Mapper接口
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Mapper
public interface CartMapper extends BaseMapper<CartDO> {
}
