package com.campus.trading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trading.common.PageResult;
import com.campus.trading.dto.CommentRequest;
import com.campus.trading.entity.ItemComment;
import com.campus.trading.entity.SecondHandItem;
import com.campus.trading.entity.SysUser;
import com.campus.trading.mapper.ItemCommentMapper;
import com.campus.trading.mapper.SecondHandItemMapper;
import com.campus.trading.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final SecondHandItemMapper itemMapper;

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

        // 批量查询回复目标的用户信息
        Set<Long> replyToUserIds = comments.stream()
                .map(ItemComment::getReplyToUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        Map<Long, SysUser> replyUserMap = new HashMap<>();
        if (!replyToUserIds.isEmpty()) {
            List<SysUser> replyUsers = userMapper.selectBatchIds(replyToUserIds);
            for (SysUser u : replyUsers) {
                replyUserMap.put(u.getId(), u);
            }
        }

        // 组装评论数据,包含用户信息
        return comments.stream().map(comment -> {
            Map<String, Object> map = new HashMap<>();
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

            // 添加回复目标用户昵称
            if (comment.getReplyToUserId() != null && comment.getReplyToUserId() > 0) {
                SysUser replyUser = replyUserMap.get(comment.getReplyToUserId());
                if (replyUser != null) {
                    String replyNickname = replyUser.getNickname() != null
                            ? replyUser.getNickname() : replyUser.getUsername();
                    map.put("replyToUserNickname", replyNickname);
                }
            }

            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 删除评论 (评论作者、物品卖家或管理员可删除，级联删除子回复)
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId, String role) {
        ItemComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 检查是否是物品卖家
        boolean isSeller = false;
        SecondHandItem item = itemMapper.selectById(comment.getItemId());
        if (item != null && item.getSellerId().equals(userId)) {
            isSeller = true;
        }

        // 只有评论作者、卖家或管理员可以删除
        boolean isAdmin = "ADMIN".equals(role);
        if (!comment.getUserId().equals(userId) && !isAdmin && !isSeller) {
            throw new RuntimeException("无权删除该评论");
        }

        // 级联删除所有子回复
        LambdaQueryWrapper<ItemComment> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(ItemComment::getParentId, commentId);
        commentMapper.delete(childWrapper);

        // 删除自身
        commentMapper.deleteById(commentId);
    }

    /**
     * 获取卖家收到的所有留言(其物品下的所有评论)
     */
    public List<Map<String, Object>> getMyMessages(Long sellerId) {
        List<ItemComment> comments = commentMapper.selectBySellerId(sellerId);

        // 批量查询回复目标的用户信息
        Set<Long> replyToUserIds = comments.stream()
                .map(ItemComment::getReplyToUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        Map<Long, SysUser> replyUserMap = new HashMap<>();
        if (!replyToUserIds.isEmpty()) {
            List<SysUser> replyUsers = userMapper.selectBatchIds(replyToUserIds);
            for (SysUser u : replyUsers) {
                replyUserMap.put(u.getId(), u);
            }
        }

        return comments.stream().map(comment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("itemId", comment.getItemId());
            map.put("userId", comment.getUserId());
            map.put("parentId", comment.getParentId());
            map.put("replyToUserId", comment.getReplyToUserId());
            map.put("content", comment.getContent());
            map.put("createTime", comment.getCreateTime());
            map.put("username", comment.getUsername());
            map.put("nickname", comment.getNickname());
            map.put("avatar", comment.getAvatar());
            map.put("itemTitle", comment.getItemTitle());
            map.put("parentContent", comment.getParentContent());

            if (comment.getReplyToUserId() != null && comment.getReplyToUserId() > 0) {
                SysUser replyUser = replyUserMap.get(comment.getReplyToUserId());
                if (replyUser != null) {
                    String replyNickname = replyUser.getNickname() != null
                            ? replyUser.getNickname() : replyUser.getUsername();
                    map.put("replyToUserNickname", replyNickname);
                }
            }
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 管理员分页查询所有留言(支持时间筛选)
     */
    public PageResult<Map<String, Object>> getAllComments(Long current, Long size,
                                                          String startTime, String endTime) {
        Page<ItemComment> page = new Page<>(current, size);
        IPage<ItemComment> result = commentMapper.selectPageForAdmin(page, startTime, endTime);

        // 批量查询回复目标用户信息
        Set<Long> replyToUserIds = result.getRecords().stream()
                .map(ItemComment::getReplyToUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        Map<Long, SysUser> replyUserMap = new HashMap<>();
        if (!replyToUserIds.isEmpty()) {
            List<SysUser> replyUsers = userMapper.selectBatchIds(replyToUserIds);
            for (SysUser u : replyUsers) {
                replyUserMap.put(u.getId(), u);
            }
        }

        List<Map<String, Object>> records = result.getRecords().stream().map(comment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("itemId", comment.getItemId());
            map.put("userId", comment.getUserId());
            map.put("parentId", comment.getParentId());
            map.put("replyToUserId", comment.getReplyToUserId());
            map.put("content", comment.getContent());
            map.put("createTime", comment.getCreateTime());
            map.put("username", comment.getUsername());
            map.put("nickname", comment.getNickname());
            map.put("avatar", comment.getAvatar());
            map.put("itemTitle", comment.getItemTitle());
            map.put("parentContent", comment.getParentContent());

            if (comment.getReplyToUserId() != null && comment.getReplyToUserId() > 0) {
                SysUser replyUser = replyUserMap.get(comment.getReplyToUserId());
                if (replyUser != null) {
                    String replyNickname = replyUser.getNickname() != null
                            ? replyUser.getNickname() : replyUser.getUsername();
                    map.put("replyToUserNickname", replyNickname);
                }
            }
            return map;
        }).collect(Collectors.toList());

        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }
}
