package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.ProductBrowseHistoryDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品浏览历史Mapper
 *
 * @author xiu
 */
@Mapper
public interface ProductBrowseHistoryMapper extends BaseMapper<ProductBrowseHistoryDO> {
}
