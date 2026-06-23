package com.campus.trading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trading.entity.SecondHandItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 二手物品Mapper
 */
@Mapper
public interface SecondHandItemMapper extends BaseMapper<SecondHandItem> {

    /**
     * 分页查询物品(支持关键词和分类搜索,按标签+标题+分类名匹配并排序)
     */
    @Select("<script>" +
            "SELECT i.*, " +
            "CASE " +
            "WHEN i.title = #{keyword} THEN 100 " +
            "WHEN i.title LIKE CONCAT('%', #{keyword}, '%') THEN 80 " +
            "WHEN FIND_IN_SET(#{keyword}, REPLACE(i.tags, '，', ',')) > 0 THEN 60 " +
            "WHEN c.category_name = #{keyword} THEN 50 " +
            "WHEN REPLACE(i.tags, '，', ',') LIKE CONCAT('%', #{keyword}, '%') THEN 40 " +
            "WHEN c.category_name LIKE CONCAT('%', #{keyword}, '%') THEN 30 " +
            "ELSE 0 END AS relevance " +
            "FROM second_hand_item i " +
            "LEFT JOIN item_category c ON i.category_id = c.id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (i.title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR FIND_IN_SET(#{keyword}, REPLACE(i.tags, '，', ',')) > 0 " +
            "OR REPLACE(i.tags, '，', ',') LIKE CONCAT('%', #{keyword}, '%') " +
            "OR c.category_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='categoryId != null'>" +
            "AND i.category_id = #{categoryId} " +
            "</if>" +
            "<if test='status != null'>" +
            "AND i.status = #{status} " +
            "</if>" +
            "<if test='sellerId != null'>" +
            "AND i.seller_id = #{sellerId} " +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "ORDER BY relevance DESC, i.publish_time DESC" +
            "</if>" +
            "<if test='keyword == null or keyword == \"\"'>" +
            "ORDER BY i.publish_time DESC" +
            "</if>" +
            "</script>")
    IPage<SecondHandItem> selectPageWithConditions(Page<SecondHandItem> page,
                                                    @Param("keyword") String keyword,
                                                    @Param("categoryId") Long categoryId,
                                                    @Param("status") Integer status,
                                                    @Param("sellerId") Long sellerId);
}
