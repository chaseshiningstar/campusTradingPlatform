package com.campus.trading.websocket;

import com.campus.trading.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * WebSocket握手拦截器
 * <p>
 * 从URL查询参数中读取token并校验,通过后将userId/username写入session.attributes
 * 客户端连接示例: ws://host:port/ws/chat?token=xxx
 * <p>
 * 注意: 浏览器原生WebSocket API不支持自定义Header,因此token只能通过query参数传递
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        URI uri = request.getURI();
        String query = uri.getQuery();
        if (query == null) {
            log.warn("WebSocket握手失败: 缺少token参数, uri={}", uri);
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        String token = null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if ("token".equals(kv[0]) && kv.length == 2) {
                token = kv[1];
                break;
            }
        }
        if (token == null || token.isEmpty()) {
            log.warn("WebSocket握手失败: token为空, uri={}", uri);
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            log.warn("WebSocket握手失败: token无效或已过期");
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        if (userId == null) {
            log.warn("WebSocket握手失败: token中无userId");
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put("userId", userId);
        attributes.put("username", username);
        log.info("WebSocket握手成功: userId={}, username={}", userId, username);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // 无需后置处理
    }
}
