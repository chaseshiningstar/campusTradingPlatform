package com.campus.trading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trading.entity.ItemComment;
import org.apache.ibatis.annotations.Mapper;
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
}
