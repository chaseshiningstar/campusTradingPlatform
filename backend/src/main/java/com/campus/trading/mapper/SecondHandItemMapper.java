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
     * 分页查询物品(支持关键词和分类搜索)
     */
    @Select("<script>" +
            "SELECT * FROM second_hand_item WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='categoryId != null'>" +
            "AND category_id = #{categoryId} " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "<if test='sellerId != null'>" +
            "AND seller_id = #{sellerId} " +
            "</if>" +
            "ORDER BY publish_time DESC" +
            "</script>")
    IPage<SecondHandItem> selectPageWithConditions(Page<SecondHandItem> page,
                                                    @Param("keyword") String keyword,
                                                    @Param("categoryId") Long categoryId,
                                                    @Param("status") Integer status,
                                                    @Param("sellerId") Long sellerId);
}
