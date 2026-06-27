package com.campus.trading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trading.entity.ItemComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 物品评论Mapper
 */
@Mapper
public interface ItemCommentMapper extends BaseMapper<ItemComment> {

    /**
     * 根据物品ID查询评论列表(包含回复)
     */
    @Select("SELECT c.*, u.username, u.nickname, u.avatar " +
            "FROM item_comment c " +
            "LEFT JOIN sys_user u ON c.user_id = u.id " +
            "WHERE c.item_id = #{itemId} AND c.status = 1 " +
            "ORDER BY c.create_time ASC")
    List<ItemComment> selectByItemIdWithUser(Long itemId);

    /**
     * 查询卖家收到的所有留言(含用户、物品、父留言信息)
     */
    @Select("SELECT c.*, u.username, u.nickname, u.avatar, i.title AS itemTitle, " +
            "p.content AS parentContent " +
            "FROM item_comment c " +
            "LEFT JOIN sys_user u ON c.user_id = u.id " +
            "LEFT JOIN second_hand_item i ON c.item_id = i.id " +
            "LEFT JOIN item_comment p ON c.parent_id = p.id " +
            "WHERE i.seller_id = #{sellerId} AND c.status = 1 " +
            "ORDER BY c.create_time DESC")
    List<ItemComment> selectBySellerId(@Param("sellerId") Long sellerId);

    /**
     * 管理员分页查询所有留言(含用户、物品、父留言信息，支持时间筛选)
     */
    @Select("<script>" +
            "SELECT c.*, u.username, u.nickname, u.avatar, i.title AS itemTitle, " +
            "p.content AS parentContent " +
            "FROM item_comment c " +
            "LEFT JOIN sys_user u ON c.user_id = u.id " +
            "LEFT JOIN second_hand_item i ON c.item_id = i.id " +
            "LEFT JOIN item_comment p ON c.parent_id = p.id " +
            "WHERE 1=1 " +
            "<if test='startTime != null and startTime != \"\"'>" +
            "AND c.create_time &gt;= #{startTime} " +
            "</if>" +
            "<if test='endTime != null and endTime != \"\"'>" +
            "AND c.create_time &lt;= #{endTime} " +
            "</if>" +
            "ORDER BY c.create_time DESC" +
            "</script>")
    IPage<ItemComment> selectPageForAdmin(Page<ItemComment> page,
                                          @Param("startTime") String startTime,
                                          @Param("endTime") String endTime);
}
