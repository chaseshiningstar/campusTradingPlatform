package com.campus.trading.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物品图片实体
 */
@Data
@TableName("item_image")
public class ItemImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 是否封面: 0-否, 1-是
     */
    private Integer isCover;

    /**
     * 排序号
     */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
