package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.ProductCategoryDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类Mapper接口
 * 
 * @author xiu
 * @date 2024/01/01
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategoryDO> {
}
