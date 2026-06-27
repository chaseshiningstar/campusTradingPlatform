-- v1.1 升级脚本: 新增私信功能 + 邮箱验证码功能
-- 适用于已部署 v1.0 数据库的环境
USE campus_trading;

-- 私信消息表
CREATE TABLE IF NOT EXISTS `private_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者用户ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者用户ID',
    `content` VARCHAR(500) NOT NULL COMMENT '消息内容',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_is_read` (`receiver_id`, `is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信消息表';

-- 邮箱验证码表
CREATE TABLE IF NOT EXISTS `email_verification_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    `code` VARCHAR(6) NOT NULL COMMENT '验证码',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `used` TINYINT DEFAULT 0 COMMENT '是否已使用: 0-未使用, 1-已使用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_email` (`email`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证码表';

-- 清空历史商品中的 contact_info 字段(不再公开展示联系方式)
UPDATE `second_hand_item` SET `contact_info` = NULL WHERE `contact_info` IS NOT NULL;
