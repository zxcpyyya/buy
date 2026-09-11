package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.UserSearchHistoryDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户搜索历史Mapper
 *
 * @author xiu
 */
@Mapper
public interface UserSearchHistoryMapper extends BaseMapper<UserSearchHistoryDO> {
}
