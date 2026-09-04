package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 
 * @author xiu
 * @date 2026/09/03
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}
