package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件分片详情实体
 * 记录每个分片的详细信息
 *
 * @author system
 * @date 2026/09/14
 */
@Data
@TableName("file_chunk_detail")
public class FileChunkDetailDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文件唯一标识（关联FileChunkRecordDO）
     */
    private String fileKey;

    /**
     * 分片序号（从1开始）
     */
    private Integer chunkIndex;

    /**
     * 分片大小（字节）
     */
    private Long chunkSize;

    /**
     * 分片文件存储路径
     */
    private String chunkPath;

    /**
     * 分片MD5（用于校验）
     */
    private String chunkMd5;

    /**
     * 是否已上传：0-未上传，1-已上传
     */
    private Integer uploaded;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}
