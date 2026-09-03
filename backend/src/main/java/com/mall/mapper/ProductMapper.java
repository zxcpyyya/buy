package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.ProductDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品Mapper接口
 * 
 * @author mall
 * @date 2024/01/01
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductDO> {
}
