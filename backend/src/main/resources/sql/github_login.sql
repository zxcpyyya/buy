-- GitHub 第三方登录字段
-- 为 sys_user 表添加第三方登录相关字段

ALTER TABLE `sys_user`
ADD COLUMN IF NOT EXISTS `github_id` VARCHAR(64) DEFAULT NULL COMMENT 'GitHub用户ID' AFTER `deleted`,
ADD COLUMN IF NOT EXISTS `github_username` VARCHAR(100) DEFAULT NULL COMMENT 'GitHub用户名' AFTER `github_id`,
ADD COLUMN IF NOT EXISTS `login_type` TINYINT NOT NULL DEFAULT 0 COMMENT '登录类型：0-本地账号，1-GitHub，2-微信，3-QQ' AFTER `github_username`;

-- 创建索引
CREATE INDEX IF NOT EXISTS `idx_github_id` ON `sys_user`(`github_id`);
CREATE INDEX IF NOT EXISTS `idx_login_type` ON `sys_user`(`login_type`);
