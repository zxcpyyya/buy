package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.SysOperationLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 *
 * @author mall
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLogDO> {
}
