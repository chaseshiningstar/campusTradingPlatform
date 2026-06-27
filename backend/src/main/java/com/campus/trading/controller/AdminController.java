package com.campus.trading.controller;

import com.campus.trading.common.PageResult;
import com.campus.trading.common.Result;
import com.campus.trading.dto.AuditRequest;
import com.campus.trading.dto.DashboardStats;
import com.campus.trading.entity.SecondHandItem;
import com.campus.trading.entity.SysUser;
import com.campus.trading.interceptor.JwtInterceptor;
import com.campus.trading.service.AdminService;
import com.campus.trading.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员控制器
 */
@Tag(name = "后台管理", description = "管理员接口")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final CommentService commentService;

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/dashboard")
    public Result<DashboardStats> getDashboardStats() {
        try {
            DashboardStats stats = adminService.getDashboardStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取待审核物品列表")
    @GetMapping("/item/audit-list")
    public Result<PageResult<SecondHandItem>> getAuditItemList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        try {
            PageResult<SecondHandItem> result = adminService.getAuditItemList(current, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "审核物品")
    @PostMapping("/item/audit")
    public Result<Void> auditItem(@Valid @RequestBody AuditRequest request, HttpServletRequest httpRequest) {
        try {
            Long auditorId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_KEY);
            adminService.auditItem(request, auditorId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取用户列表")
    @GetMapping("/user/list")
    public Result<PageResult<SysUser>> getUserList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        try {
            PageResult<SysUser> result = adminService.getUserList(current, size, keyword);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "封禁/解封用户")
    @PutMapping("/user/toggle-status/{id}")
    public Result<Void> toggleUserStatus(@PathVariable Long id) {
        try {
            adminService.toggleUserStatus(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "管理员下架物品")
    @PutMapping("/item/offline/{id}")
    public Result<Void> adminOfflineItem(@PathVariable Long id,
                                         @RequestParam(required = false) String reason,
                                         HttpServletRequest httpRequest) {
        try {
            Long auditorId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_KEY);
            adminService.adminOfflineItem(id, auditorId, reason);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "管理员分页查询所有留言(支持时间筛选)")
    @GetMapping("/comments")
    public Result<PageResult<Map<String, Object>>> getCommentList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            PageResult<Map<String, Object>> result = commentService.getAllComments(current, size, startTime, endTime);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
