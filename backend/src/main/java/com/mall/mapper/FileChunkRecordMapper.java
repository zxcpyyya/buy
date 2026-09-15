package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.FileChunkRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件分片记录 Mapper
 *
 * @author system
 * @date 2026/09/14
 */
@Mapper
public interface FileChunkRecordMapper extends BaseMapper<FileChunkRecordDO> {
}
