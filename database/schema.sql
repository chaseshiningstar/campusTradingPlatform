-- 校园二手交易平台数据库脚本
-- MySQL 8.0+

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS campus_trading DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_trading;

-- 1. 用户表
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 角色表
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 3. 用户角色关联表
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 4. 物品分类表
CREATE TABLE `item_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID, 0表示一级分类',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品分类表';

-- 5. 二手物品表
CREATE TABLE `second_hand_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '物品ID',
    `title` VARCHAR(50) NOT NULL COMMENT '物品标题',
    `description` TEXT COMMENT '物品描述',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `original_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '原价',
    `condition_level` TINYINT DEFAULT 1 COMMENT '新旧程度: 1-全新, 2-九成新, 3-八成新, 4-七成新, 5-六成新及以下',
    `size` VARCHAR(30) DEFAULT NULL COMMENT '尺码/码数(服装类必填)',
    `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
    `contact_info` VARCHAR(255) DEFAULT NULL COMMENT '联系方式',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待审核, 1-已发布, 2-已下架, 3-已售出, 4-审核驳回',
    `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `tags` VARCHAR(100) DEFAULT NULL COMMENT '商品标签,逗号分隔(最多6个)',
    `publish_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_status` (`status`),
    KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二手物品表';

-- 6. 物品图片表
CREATE TABLE `item_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `item_id` BIGINT NOT NULL COMMENT '物品ID',
    `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
    `is_cover` TINYINT DEFAULT 0 COMMENT '是否封面: 0-否, 1-是',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品图片表';

-- 7. 物品评论/留言表
CREATE TABLE `item_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `item_id` BIGINT NOT NULL COMMENT '物品ID',
    `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID, 0表示一级评论',
    `reply_to_user_id` BIGINT DEFAULT NULL COMMENT '回复的用户ID',
    `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-隐藏, 1-显示',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品评论表';

-- 8. 审核记录表
CREATE TABLE `audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审核记录ID',
    `item_id` BIGINT NOT NULL COMMENT '物品ID',
    `auditor_id` BIGINT NOT NULL COMMENT '审核员ID',
    `action` TINYINT NOT NULL COMMENT '操作: 1-通过, 2-驳回, 3-下架',
    `reason` VARCHAR(255) DEFAULT NULL COMMENT '审核原因/备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    PRIMARY KEY (`id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_auditor_id` (`auditor_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录表';

-- 插入初始数据

-- 默认角色
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`) VALUES
('普通用户', 'USER', '普通用户角色'),
('管理员', 'ADMIN', '管理员角色');

-- 默认管理员账号 (密码: 123456)
-- 提示: 如果此哈希无效,请先注册一个普通账号,再通过以下SQL替换:
--   UPDATE sys_user u1, sys_user u2 SET u1.password = u2.password WHERE u1.username = 'admin' AND u2.username = '你的账号';
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`) VALUES
('admin', '$2a$10$OgKyP18oUDX.3Z5j/xPGf.C3oA3uWUYVeBlMHXdviEtMmslxUexPS', '系统管理员', 'admin@campus.com', 1);

-- 分配管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 2);

-- 物品分类
INSERT INTO `item_category` (`category_name`, `parent_id`, `sort_order`, `icon`) VALUES
('电子产品', 0, 1, 'electronics'),
('图书教材', 0, 2, 'books'),
('生活用品', 0, 3, 'daily'),
('服装鞋帽', 0, 4, 'clothing'),
('运动健身', 0, 5, 'sports'),
('其他', 0, 6, 'other');

INSERT INTO `item_category` (`category_name`, `parent_id`, `sort_order`, `icon`) VALUES
('手机', 1, 1, 'phone'),
('电脑', 1, 2, 'computer'),
('平板', 1, 3, 'tablet'),
('数码配件', 1, 4, 'accessories');

INSERT INTO `item_category` (`category_name`, `parent_id`, `sort_order`, `icon`) VALUES
('教材', 2, 1, 'textbook'),
('小说', 2, 2, 'novel'),
('杂志', 2, 3, 'magazine');

SET FOREIGN_KEY_CHECKS = 1;
