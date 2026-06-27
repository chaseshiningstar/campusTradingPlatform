package com.campus.trading.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信消息实体
 */
@Data
@TableName("private_message")
public class PrivateMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long senderId;

    private Long receiverId;

    private String content;

    /**
     * 是否已读: 0-未读, 1-已读
     */
    private Integer isRead;

    private LocalDateTime createTime;

    // ============ 非数据库字段(用于联表查询返回) ============

    /**
     * 发送者用户名(非数据库字段)
     */
    @TableField(exist = false)
    private String senderUsername;

    /**
     * 发送者昵称(非数据库字段)
     */
    @TableField(exist = false)
    private String senderNickname;

    /**
     * 发送者头像(非数据库字段)
     */
    @TableField(exist = false)
    private String senderAvatar;

    /**
     * 接收者用户名(非数据库字段)
     */
    @TableField(exist = false)
    private String receiverUsername;

    /**
     * 接收者昵称(非数据库字段)
     */
    @TableField(exist = false)
    private String receiverNickname;

    /**
     * 接收者头像(非数据库字段)
     */
    @TableField(exist = false)
    private String receiverAvatar;
}
