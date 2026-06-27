package com.campus.trading.websocket;

import com.campus.trading.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 私信WebSocket处理器
 * <p>
 * 设计要点:
 * 1. 一个用户可能同时打开多个标签页/设备,因此每个userId对应一组session
 * 2. 通过JWT token在握手时认证(由拦截器在URL query参数中校验)
 * 3. session的attributes中保存userId,便于后续推送
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;

    /**
     * 用户ID -> 该用户的所有WebSocket会话(支持多设备同时在线)
     */
    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            try {
                session.close(CloseStatus.NOT_ACCEPTABLE);
            } catch (IOException ignored) {
            }
            return;
        }
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("WebSocket已连接: userId={}, sessionId={}, 在线会话数={}",
                userId, session.getId(), userSessions.get(userId).size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 私信客户端仅作为接收端,业务消息发送通过REST接口 /message/send 进行(确保入库)
        // 此处可处理客户端心跳或ack消息,暂不处理业务payload
        String payload = message.getPayload();
        if ("ping".equalsIgnoreCase(payload)) {
            try {
                session.sendMessage(new TextMessage("pong"));
            } catch (IOException e) {
                log.warn("发送pong失败: {}", e.getMessage());
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            return;
        }
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
        log.info("WebSocket已断开: userId={}, sessionId={}, status={}", userId, session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = (Long) session.getAttributes().get("userId");
        log.error("WebSocket传输错误: userId={}, sessionId={}, error={}",
                userId, session.getId(), exception.getMessage());
        if (session.isOpen()) {
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 向指定用户的所有在线会话推送消息
     *
     * @param userId  接收者用户ID
     * @param payload JSON格式消息体
     * @return true=至少有一个会话成功推送; false=用户不在线或推送全部失败
     */
    public boolean sendToUser(Long userId, String payload) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }
        TextMessage textMessage = new TextMessage(payload);
        boolean anySuccess = false;
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    // 同步发送避免并发写冲突
                    synchronized (session) {
                        session.sendMessage(textMessage);
                    }
                    anySuccess = true;
                } catch (IOException e) {
                    log.warn("推送消息失败: userId={}, sessionId={}, error={}",
                            userId, session.getId(), e.getMessage());
                }
            }
        }
        return anySuccess;
    }

    /**
     * 判断用户是否在线(至少一个会话存活)
     */
    public boolean isUserOnline(Long userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }
        return sessions.stream().anyMatch(WebSocketSession::isOpen);
    }
}
