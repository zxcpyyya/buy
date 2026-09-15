-- 文件分片上传记录表
CREATE TABLE IF NOT EXISTS `file_chunk_record` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `file_key` VARCHAR(128) NOT NULL COMMENT '文件唯一标识（UUID）',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名称',
    `total_size` BIGINT NOT NULL COMMENT '文件总大小（字节）',
    `chunk_size` BIGINT NOT NULL COMMENT '分片大小（字节）',
    `total_chunks` INT NOT NULL COMMENT '总分片数',
    `uploaded_chunks` INT NOT NULL DEFAULT 0 COMMENT '已上传的分片数',
    `file_md5` VARCHAR(64) NOT NULL COMMENT '文件MD5',
    `file_path` VARCHAR(512) DEFAULT NULL COMMENT '文件相对存储路径（合并后）',
    `file_url` VARCHAR(512) DEFAULT NULL COMMENT '文件访问URL',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '上传状态：0-上传中，1-上传完成，2-上传失败',
    `user_id` BIGINT NOT NULL COMMENT '上传用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    INDEX `idx_file_key` (`file_key`),
    INDEX `idx_file_md5` (`file_md5`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件分片上传记录表';

-- 文件分片详情表
CREATE TABLE IF NOT EXISTS `file_chunk_detail` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `file_key` VARCHAR(128) NOT NULL COMMENT '文件唯一标识（关联file_chunk_record）',
    `chunk_index` INT NOT NULL COMMENT '分片序号（从1开始）',
    `chunk_size` BIGINT NOT NULL COMMENT '分片大小（字节）',
    `chunk_path` VARCHAR(512) NOT NULL COMMENT '分片文件存储路径',
    `chunk_md5` VARCHAR(64) DEFAULT NULL COMMENT '分片MD5',
    `uploaded` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已上传：0-未上传，1-已上传',
    `upload_time` DATETIME DEFAULT NULL COMMENT '上传时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    INDEX `idx_file_key` (`file_key`),
    INDEX `idx_chunk_index` (`chunk_index`),
    UNIQUE INDEX `uk_file_chunk` (`file_key`, `chunk_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件分片详情表';
