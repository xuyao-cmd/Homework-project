/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50505
Source Host           : localhost:3308
Source Database       : cloud_disk

Target Server Type    : MYSQL
Target Server Version : 50505
File Encoding         : 65001

Date: 2026-06-16 13:42:53
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for file
-- ----------------------------
DROP TABLE IF EXISTS `file`;
CREATE TABLE `file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `md5` varchar(64) DEFAULT NULL COMMENT '文件MD5值(用于秒传)',
  `name` varchar(256) NOT NULL COMMENT '原始文件名',
  `size` bigint(20) DEFAULT 0 COMMENT '文件大小(字节)',
  `path` varchar(512) NOT NULL COMMENT '存储路径',
  `type` varchar(32) DEFAULT 'other' COMMENT '文件类型(扩展名)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_md5` (`md5`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物理文件表';

-- ----------------------------
-- Records of file
-- ----------------------------

-- ----------------------------
-- Table structure for share
-- ----------------------------
DROP TABLE IF EXISTS `share`;
CREATE TABLE `share` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分享ID',
  `user_id` bigint(20) NOT NULL COMMENT '分享用户ID',
  `file_id` bigint(20) NOT NULL COMMENT '分享的文件/目录ID(关联user_file.id)',
  `share_code` varchar(16) NOT NULL COMMENT '分享码',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间(NULL=永不过期)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_share_code` (`share_code`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分享表';

-- ----------------------------
-- Records of share
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '密码(MD5加密)',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(512) DEFAULT NULL COMMENT '头像URL',
  `oauth_platform` varchar(32) DEFAULT NULL COMMENT '第三方登录平台(github)',
  `oauth_open_id` varchar(128) DEFAULT NULL COMMENT '第三方登录唯一标识',
  `role` varchar(16) DEFAULT 'user' COMMENT '角色: user-普通用户, admin-管理员',
  `status` int(11) DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_oauth` (`oauth_platform`,`oauth_open_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', 'admin', '0192023a7bbd73250516f069df18b500', 'admin@cloud-disk.com', null, null, null, 'admin', '1', '2026-06-16 13:31:31', '2026-06-16 13:31:31');

-- ----------------------------
-- Table structure for user_file
-- ----------------------------
DROP TABLE IF EXISTS `user_file`;
CREATE TABLE `user_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `file_id` bigint(20) DEFAULT NULL COMMENT '关联物理文件ID(文件夹为NULL)',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父目录ID(0=根目录)',
  `file_name` varchar(256) NOT NULL COMMENT '文件/文件夹名',
  `is_deleted` int(11) DEFAULT 0 COMMENT '是否删除: 0-否, 1-回收站',
  `deleted_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_dir` int(11) DEFAULT 0 COMMENT '是否目录: 0-文件, 1-目录',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_parent` (`user_id`,`parent_id`,`is_deleted`),
  KEY `idx_user_deleted` (`user_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户文件表';

-- ----------------------------
-- Records of user_file
-- ----------------------------
