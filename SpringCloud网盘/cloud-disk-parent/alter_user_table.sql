-- 给 user 表添加缺失的字段
USE cloud_disk;

-- 添加 oauth_platform 列
ALTER TABLE `user` ADD COLUMN `oauth_platform` VARCHAR(32) DEFAULT NULL COMMENT '第三方登录平台(github/wechat/qq)' AFTER `avatar`;

-- 添加 oauth_open_id 列
ALTER TABLE `user` ADD COLUMN `oauth_open_id` VARCHAR(128) DEFAULT NULL COMMENT '第三方登录唯一标识' AFTER `oauth_platform`;

-- 添加 role 列（如果不存在）
ALTER TABLE `user` ADD COLUMN `role` VARCHAR(16) DEFAULT 'user' COMMENT '角色: user-普通用户, admin-管理员' AFTER `oauth_open_id`;

-- 添加 status 列（如果不存在）
ALTER TABLE `user` ADD COLUMN `status` INT DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用' AFTER `role`;

-- 给已有用户设置默认值
UPDATE `user` SET `role` = 'user' WHERE `role` IS NULL;
UPDATE `user` SET `status` = 1 WHERE `status` IS NULL;

-- 添加 oauth 唯一索引
ALTER TABLE `user` ADD UNIQUE INDEX `uk_oauth` (`oauth_platform`, `oauth_open_id`);
