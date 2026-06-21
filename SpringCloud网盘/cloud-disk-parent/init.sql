-- 云盘系统数据库初始化脚本
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS cloud_disk DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cloud_disk;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(128) NOT NULL COMMENT '密码(MD5加密)',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    `oauth_platform` VARCHAR(32) DEFAULT NULL COMMENT '第三方登录平台(github)',
    `oauth_open_id` VARCHAR(128) DEFAULT NULL COMMENT '第三方登录唯一标识',
    `role` VARCHAR(16) DEFAULT 'user' COMMENT '角色: user-普通用户, admin-管理员',
    `status` INT DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_oauth` (`oauth_platform`, `oauth_open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员账号: admin / admin123
INSERT INTO `user` (`username`, `password`, `email`, `role`, `status`, `create_time`, `update_time`)
VALUES ('admin', MD5('admin123'), 'admin@cloud-disk.com', 'admin', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `username`=`username`;

-- 物理文件表
CREATE TABLE IF NOT EXISTS `file` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `md5` VARCHAR(64) DEFAULT NULL COMMENT '文件MD5值(用于秒传)',
    `name` VARCHAR(256) NOT NULL COMMENT '原始文件名',
    `size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `path` VARCHAR(512) NOT NULL COMMENT '存储路径',
    `type` VARCHAR(32) DEFAULT 'other' COMMENT '文件类型(扩展名)',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_md5` (`md5`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物理文件表';

-- 用户文件表
CREATE TABLE IF NOT EXISTS `user_file` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `file_id` BIGINT DEFAULT NULL COMMENT '关联物理文件ID(文件夹为NULL)',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父目录ID(0=根目录)',
    `file_name` VARCHAR(256) NOT NULL COMMENT '文件/文件夹名',
    `is_deleted` INT DEFAULT 0 COMMENT '是否删除: 0-否, 1-回收站',
    `deleted_time` DATETIME DEFAULT NULL COMMENT '删除时间',
    `is_dir` INT DEFAULT 0 COMMENT '是否目录: 0-文件, 1-目录',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_parent` (`user_id`, `parent_id`, `is_deleted`),
    KEY `idx_user_deleted` (`user_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户文件表';

-- 分享表
CREATE TABLE IF NOT EXISTS `share` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分享ID',
    `user_id` BIGINT NOT NULL COMMENT '分享用户ID',
    `file_id` BIGINT NOT NULL COMMENT '分享的文件/目录ID(关联user_file.id)',
    `share_code` VARCHAR(16) NOT NULL COMMENT '分享码',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间(NULL=永不过期)',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_share_code` (`share_code`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享表';
