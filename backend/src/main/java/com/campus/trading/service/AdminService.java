package com.campus.trading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trading.common.PageResult;
import com.campus.trading.dto.AuditRequest;
import com.campus.trading.dto.DashboardStats;
import com.campus.trading.entity.AuditLog;
import com.campus.trading.entity.ItemComment;
import com.campus.trading.entity.ItemImage;
import com.campus.trading.entity.SecondHandItem;
import com.campus.trading.entity.SysUser;
import com.campus.trading.mapper.AuditLogMapper;
import com.campus.trading.mapper.ItemCommentMapper;
import com.campus.trading.mapper.ItemImageMapper;
import com.campus.trading.mapper.SecondHandItemMapper;
import com.campus.trading.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final SecondHandItemMapper itemMapper;
    private final ItemImageMapper imageMapper;
    private final AuditLogMapper auditLogMapper;
    private final SysUserMapper userMapper;
    private final ItemCommentMapper commentMapper;

    /**
     * 分页查询待审核物品
     */
    public PageResult<SecondHandItem> getAuditItemList(Long current, Long size) {
        Page<SecondHandItem> page = new Page<>(current, size);

        LambdaQueryWrapper<SecondHandItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SecondHandItem::getStatus, 0) // 待审核
                .orderByDesc(SecondHandItem::getCreateTime);

        IPage<SecondHandItem> result = itemMapper.selectPage(page, wrapper);

        // 为每个物品设置封面图
        List<SecondHandItem> records = result.getRecords();
        for (SecondHandItem item : records) {
            List<ItemImage> images = imageMapper.selectByItemId(item.getId());
            if (images != null && !images.isEmpty()) {
                // 找到封面图
                ItemImage cover = images.stream()
                        .filter(img -> img.getIsCover() == 1)
                        .findFirst()
                        .orElse(images.get(0));
                item.setCoverImage(cover.getImageUrl());
            }
        }

        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 审核物品
     */
    @Transactional
    public void auditItem(AuditRequest request, Long auditorId) {
        SecondHandItem item = itemMapper.selectById(request.getItemId());
        if (item == null) {
            throw new RuntimeException("物品不存在");
        }

        // 更新物品状态
        if (request.getAction() == 1) {
            // 通过
            item.setStatus(1);
            item.setRejectReason(null);
        } else if (request.getAction() == 2) {
            // 驳回
            item.setStatus(4);
            item.setRejectReason(request.getReason());
        } else if (request.getAction() == 3) {
            // 下架
            item.setStatus(2);
        }

        itemMapper.updateById(item);

        // 记录审核日志
        AuditLog auditLog = new AuditLog();
        auditLog.setItemId(request.getItemId());
        auditLog.setAuditorId(auditorId);
        auditLog.setAction(request.getAction());
        auditLog.setReason(request.getReason());

        auditLogMapper.insert(auditLog);
    }

    /**
     * 分页查询用户列表
     */
    public PageResult<SysUser> getUserList(Long current, Long size, String keyword) {
        Page<SysUser> page = new Page<>(current, size);

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getNickname, keyword);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        IPage<SysUser> result = userMapper.selectPage(page, wrapper);

        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 封禁/解封用户
     */
    public void toggleUserStatus(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(user);
    }

    /**
     * 管理员下架物品
     */
    @Transactional
    public void adminOfflineItem(Long itemId, Long auditorId, String reason) {
        SecondHandItem item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new RuntimeException("物品不存在");
        }

        item.setStatus(2); // 已下架
        itemMapper.updateById(item);

        // 记录审核日志
        AuditLog auditLog = new AuditLog();
        auditLog.setItemId(itemId);
        auditLog.setAuditorId(auditorId);
        auditLog.setAction(3); // 下架
        auditLog.setReason(reason);
        auditLogMapper.insert(auditLog);
    }

    /**
     * 获取仪表盘统计数据
     */
    public DashboardStats getDashboardStats() {
        // 物品总数
        Long itemCount = itemMapper.selectCount(null);

        // 待审核数量
        LambdaQueryWrapper<SecondHandItem> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(SecondHandItem::getStatus, 0);
        Long pendingAudit = itemMapper.selectCount(pendingWrapper);

        // 用户总数
        Long userCount = userMapper.selectCount(null);

        // 评论总数
        Long commentCount = commentMapper.selectCount(null);

        return new DashboardStats(itemCount, pendingAudit, userCount, commentCount);
    }
}
