package com.campus.trading.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 二手物品实体
 */
@Data
@TableName("second_hand_item")
public class SecondHandItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 物品标题
     */
    private String title;

    /**
     * 物品描述
     */
    private String description;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 新旧程度: 1-全新, 2-九成新, 3-八成新, 4-七成新, 5-六成新及以下
     */
    private Integer conditionLevel;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 联系方式
     */
    private String contactInfo;

    /**
     * 状态: 0-待审核, 1-已发布, 2-已下架, 3-已售出, 4-审核驳回
     */
    private Integer status;

    /**
     * 驳回原因
     */
    private String rejectReason;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime publishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
