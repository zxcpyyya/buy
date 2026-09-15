package com.mall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 文件上传初始化响应VO
 *
 * @author system
 * @date 2026/09/14
 */
@Data
@Builder
@Schema(name = "FileUploadInitVO", description = "文件上传初始化响应")
public class FileUploadInitVO {

    @Schema(description = "文件唯一标识（用于后续上传）")
    private String fileKey;

    @Schema(description = "已上传的分片列表（用于断点续传）")
    private List<Integer> uploadedChunks;

    @Schema(description = "是否需要分片上传（true-分片，false-普通上传）")
    private Boolean needChunk;

    @Schema(description = "文件访问URL（如果已上传完成）")
    private String fileUrl;

    @Schema(description = "分片大小")
    private Long chunkSize;

    @Schema(description = "总分片数")
    private Integer totalChunks;
}
