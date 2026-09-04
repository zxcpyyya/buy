package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.CartDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车Mapper接口
 * 
 * @author xiu
 * @date 2024/01/01
 */
@Mapper
public interface CartMapper extends BaseMapper<CartDO> {
}
