package com.campus.trading.controller;

import com.campus.trading.common.Result;
import com.campus.trading.entity.SysUser;
import com.campus.trading.interceptor.JwtInterceptor;
import com.campus.trading.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户信息相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<SysUser> getUserInfo(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute(JwtInterceptor.USER_ID_KEY);
            SysUser user = userService.getUserById(userId);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/update")
    public Result<Void> updateUserInfo(@RequestBody SysUser updateUser, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute(JwtInterceptor.USER_ID_KEY);
            userService.updateUserInfo(userId, updateUser);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestParam String oldPassword,
                                       @RequestParam String newPassword,
                                       HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute(JwtInterceptor.USER_ID_KEY);
            userService.changePassword(userId, oldPassword, newPassword);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                       HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute(JwtInterceptor.USER_ID_KEY);
            String avatarUrl = userService.uploadAvatar(userId, file);
            return Result.success(avatarUrl);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
