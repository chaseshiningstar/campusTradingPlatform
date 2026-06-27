-- 修复脚本: 解决 private_message 表中 conversation_id 字段导致插入失败的问题
-- 适用于已存在带 conversation_id 字段的数据库
USE campus_trading;

-- 检查并删除 conversation_id 字段(使用存储过程兼容 MySQL 8.0)
DROP PROCEDURE IF EXISTS drop_conversation_id_if_exists;
DELIMITER $$
CREATE PROCEDURE drop_conversation_id_if_exists()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = 'campus_trading'
                 AND TABLE_NAME = 'private_message'
                 AND COLUMN_NAME = 'conversation_id') THEN
        ALTER TABLE `private_message` DROP COLUMN `conversation_id`;
        SELECT '已删除 conversation_id 字段' AS result;
    ELSE
        SELECT 'conversation_id 字段不存在,无需处理' AS result;
    END IF;
END$$
DELIMITER ;

CALL drop_conversation_id_if_exists();
DROP PROCEDURE IF EXISTS drop_conversation_id_if_exists;

-- 验证表结构(执行后应只包含: id, sender_id, receiver_id, content, is_read, create_time)
DESC `private_message`;
