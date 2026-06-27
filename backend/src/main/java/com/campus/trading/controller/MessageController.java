package com.campus.trading.controller;

import com.campus.trading.common.Result;
import com.campus.trading.dto.SendMessageRequest;
import com.campus.trading.entity.PrivateMessage;
import com.campus.trading.interceptor.JwtInterceptor;
import com.campus.trading.service.PrivateMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 私信控制器
 */
@Tag(name = "私信管理", description = "用户间私信收发接口")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final PrivateMessageService messageService;

    @Operation(summary = "发送私信")
    @PostMapping("/send")
    public Result<PrivateMessage> sendMessage(@Valid @RequestBody SendMessageRequest request,
                                               HttpServletRequest httpRequest) {
        try {
            Long senderId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_KEY);
            PrivateMessage message = messageService.sendMessage(senderId, request.getReceiverId(), request.getContent());
            return Result.success(message);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取与某用户的历史消息")
    @GetMapping("/history/{userId}")
    public Result<List<PrivateMessage>> getHistory(@PathVariable Long userId,
                                                    HttpServletRequest httpRequest) {
        try {
            Long currentUserId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_KEY);
            List<PrivateMessage> messages = messageService.getConversation(currentUserId, userId);
            // 进入会话即标记已读
            messageService.markConversationRead(currentUserId, userId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/conversations")
    public Result<List<PrivateMessage>> getConversations(HttpServletRequest httpRequest) {
        try {
            Long currentUserId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_KEY);
            List<PrivateMessage> conversations = messageService.getConversations(currentUserId);
            return Result.success(conversations);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取未读消息总数")
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> getUnreadCount(HttpServletRequest httpRequest) {
        try {
            Long currentUserId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_KEY);
            int count = messageService.countUnread(currentUserId);
            Map<String, Integer> result = new HashMap<>();
            result.put("unreadCount", count);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "标记与某用户的会话为已读")
    @PutMapping("/read/{userId}")
    public Result<Void> markRead(@PathVariable Long userId,
                                  HttpServletRequest httpRequest) {
        try {
            Long currentUserId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_KEY);
            messageService.markConversationRead(currentUserId, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
