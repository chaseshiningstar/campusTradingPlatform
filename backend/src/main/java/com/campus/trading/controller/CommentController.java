package com.campus.trading.controller;

import com.campus.trading.common.Result;
import com.campus.trading.dto.CommentRequest;
import com.campus.trading.interceptor.JwtInterceptor;
import com.campus.trading.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评论控制器
 */
@Tag(name = "评论管理", description = "物品评论相关接口")
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "获取物品评论列表")
    @GetMapping("/list/{itemId}")
    public Result<List<Map<String, Object>>> getComments(@PathVariable Long itemId) {
        try {
            List<Map<String, Object>> comments = commentService.getCommentsByItemId(itemId);
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "添加评论")
    @PostMapping("/add")
    public Result<Void> addComment(@Valid @RequestBody CommentRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_KEY);
            commentService.addComment(request, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteComment(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_KEY);
            String role = (String) httpRequest.getAttribute(JwtInterceptor.ROLE_KEY);
            commentService.deleteComment(id, userId, role);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
