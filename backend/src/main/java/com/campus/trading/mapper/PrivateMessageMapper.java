package com.campus.trading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trading.entity.PrivateMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 私信消息Mapper
 */
@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {

    /**
     * 查询两个用户之间的历史消息(按时间正序)
     * 联表查询发送者/接收者用户信息
     */
    @Select("SELECT m.*, " +
            "su.username AS sender_username, su.nickname AS sender_nickname, su.avatar AS sender_avatar, " +
            "ru.username AS receiver_username, ru.nickname AS receiver_nickname, ru.avatar AS receiver_avatar " +
            "FROM private_message m " +
            "LEFT JOIN sys_user su ON m.sender_id = su.id " +
            "LEFT JOIN sys_user ru ON m.receiver_id = ru.id " +
            "WHERE (m.sender_id = #{userId1} AND m.receiver_id = #{userId2}) " +
            "OR (m.sender_id = #{userId2} AND m.receiver_id = #{userId1}) " +
            "ORDER BY m.create_time ASC")
    List<PrivateMessage> selectConversation(@Param("userId1") Long userId1,
                                              @Param("userId2") Long userId2);

    /**
     * 查询用户的会话列表(每个对话伙伴的最新一条消息)
     * 使用子查询获取每个 (sender,receiver) 组合的最新消息
     */
    @Select("SELECT m.*, " +
            "su.username AS sender_username, su.nickname AS sender_nickname, su.avatar AS sender_avatar, " +
            "ru.username AS receiver_username, ru.nickname AS receiver_nickname, ru.avatar AS receiver_avatar " +
            "FROM private_message m " +
            "INNER JOIN ( " +
            "  SELECT MAX(id) AS max_id FROM private_message " +
            "  WHERE sender_id = #{userId} OR receiver_id = #{userId} " +
            "  GROUP BY CASE WHEN sender_id = #{userId} THEN receiver_id ELSE sender_id END " +
            ") latest ON m.id = latest.max_id " +
            "LEFT JOIN sys_user su ON m.sender_id = su.id " +
            "LEFT JOIN sys_user ru ON m.receiver_id = ru.id " +
            "ORDER BY m.create_time DESC")
    List<PrivateMessage> selectConversations(@Param("userId") Long userId);

    /**
     * 统计用户未读消息总数
     */
    @Select("SELECT COUNT(*) FROM private_message WHERE receiver_id = #{userId} AND is_read = 0")
    int countUnread(@Param("userId") Long userId);

    /**
     * 将两个用户之间的所有消息标记为已读(当前用户作为接收者)
     */
    @Update("UPDATE private_message SET is_read = 1 " +
            "WHERE receiver_id = #{receiverId} AND sender_id = #{senderId} AND is_read = 0")
    int markConversationRead(@Param("receiverId") Long receiverId,
                              @Param("senderId") Long senderId);

    /**
     * 查询指定时间之后两个用户之间的消息(用于增量拉取)
     */
    @Select("SELECT m.*, " +
            "su.username AS sender_username, su.nickname AS sender_nickname, su.avatar AS sender_avatar, " +
            "ru.username AS receiver_username, ru.nickname AS receiver_nickname, ru.avatar AS receiver_avatar " +
            "FROM private_message m " +
            "LEFT JOIN sys_user su ON m.sender_id = su.id " +
            "LEFT JOIN sys_user ru ON m.receiver_id = ru.id " +
            "WHERE ((m.sender_id = #{userId1} AND m.receiver_id = #{userId2}) " +
            "OR (m.sender_id = #{userId2} AND m.receiver_id = #{userId1})) " +
            "AND m.create_time > #{since} " +
            "ORDER BY m.create_time ASC")
    List<PrivateMessage> selectConversationSince(@Param("userId1") Long userId1,
                                                   @Param("userId2") Long userId2,
                                                   @Param("since") LocalDateTime since);
}
