package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 
 * @author xiu
 * @date 2024/01/01
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}
