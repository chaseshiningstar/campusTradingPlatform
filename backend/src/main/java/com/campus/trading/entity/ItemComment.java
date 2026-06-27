package com.campus.trading.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物品评论实体
 */
@Data
@TableName("item_comment")
public class ItemComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 父评论ID, 0表示一级评论
     */
    private Long parentId;

    /**
     * 回复的用户ID
     */
    private Long replyToUserId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 状态: 0-隐藏, 1-显示
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 以下字段非数据库字段,用于关联查询
     */
    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String avatar;

    @TableField(exist = false)
    private String itemTitle;

    @TableField(exist = false)
    private String parentContent;
}
