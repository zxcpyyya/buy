package com.mall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 分片上传结果VO
 *
 * @author system
 * @date 2026/09/14
 */
@Data
@Builder
@Schema(name = "ChunkUploadVO", description = "分片上传结果")
public class ChunkUploadVO {

    @Schema(description = "文件唯一标识")
    private String fileKey;

    @Schema(description = "当前分片序号")
    private Integer chunkIndex;

    @Schema(description = "已上传的分片数")
    private Integer uploadedChunks;

    @Schema(description = "总分片数")
    private Integer totalChunks;

    @Schema(description = "是否全部上传完成")
    private Boolean completed;

    @Schema(description = "文件访问URL（如果已完成）")
    private String fileUrl;
}
