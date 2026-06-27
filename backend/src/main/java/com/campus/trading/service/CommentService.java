package com.campus.trading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.dto.CommentRequest;
import com.campus.trading.entity.ItemComment;
import com.campus.trading.entity.SysUser;
import com.campus.trading.mapper.ItemCommentMapper;
import com.campus.trading.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final ItemCommentMapper commentMapper;
    private final SysUserMapper userMapper;

    /**
     * 添加评论
     */
    public void addComment(CommentRequest request, Long userId) {
        ItemComment comment = new ItemComment();
        comment.setItemId(request.getItemId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        comment.setReplyToUserId(request.getReplyToUserId());
        comment.setContent(request.getContent());
        comment.setStatus(1);

        commentMapper.insert(comment);
    }

    /**
     * 获取物品评论列表
     */
    public List<Map<String, Object>> getCommentsByItemId(Long itemId) {
        List<ItemComment> comments = commentMapper.selectByItemIdWithUser(itemId);

        // 组装评论数据,包含用户信息
        return comments.stream().map(comment -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", comment.getId());
            map.put("itemId", comment.getItemId());
            map.put("userId", comment.getUserId());
            map.put("parentId", comment.getParentId());
            map.put("replyToUserId", comment.getReplyToUserId());
            map.put("content", comment.getContent());
            map.put("createTime", comment.getCreateTime());

            // 添加用户信息
            map.put("username", comment.getUsername());
            map.put("nickname", comment.getNickname());
            map.put("avatar", comment.getAvatar());

            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 删除评论 (作者本人或管理员可删除)
     */
    public void deleteComment(Long commentId, Long userId, String role) {
        ItemComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 只有评论作者或管理员可以删除
        boolean isAdmin = "ADMIN".equals(role);
        if (!comment.getUserId().equals(userId) && !isAdmin) {
            throw new RuntimeException("无权删除该评论");
        }

        commentMapper.deleteById(commentId);
    }
}
