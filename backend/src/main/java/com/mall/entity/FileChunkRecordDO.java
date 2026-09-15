package com.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件分片上传记录实体
 * 用于记录分片上传的元数据和已上传的分片
 *
 * @author system
 * @date 2026/09/14
 */
@Data
@TableName("file_chunk_record")
public class FileChunkRecordDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文件唯一标识（UUID）
     * 用于标识同一个文件的多个分片
     */
    private String fileKey;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件总大小（字节）
     */
    private Long totalSize;

    /**
     * 分片大小（字节）
     */
    private Long chunkSize;

    /**
     * 总分片数
     */
    private Integer totalChunks;

    /**
     * 已上传的分片数
     */
    private Integer uploadedChunks;

    /**
     * 文件MD5（完整性校验）
     */
    private String fileMd5;

    /**
     * 文件相对存储路径（合并后）
     */
    private String filePath;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 上传状态：0-上传中，1-上传完成，2-上传失败
     */
    private Integer status;

    /**
     * 上传用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}
