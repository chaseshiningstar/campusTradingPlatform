package com.campus.trading.dto;

import lombok.Data;

/**
 * 物品查询请求DTO
 */
@Data
public class ItemQueryRequest {

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 卖家ID
     */
    private Long sellerId;
}
