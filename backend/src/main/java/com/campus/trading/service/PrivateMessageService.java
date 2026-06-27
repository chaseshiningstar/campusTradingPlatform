package com.campus.trading.service;

import com.campus.trading.entity.PrivateMessage;
import com.campus.trading.entity.SysUser;
import com.campus.trading.mapper.PrivateMessageMapper;
import com.campus.trading.mapper.SysUserMapper;
import com.campus.trading.websocket.ChatWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 私信服务
 * <p>
 * 设计要点:
 * 1. 消息发送采用"先入库,再推送"策略,确保消息持久化(多设备可用)
 * 2. 即使接收者不在线,消息也已存于数据库,下次上线可拉取
 * 3. 通过WebSocket向接收者所有在线会话推送,实现多设备同步
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateMessageService {

    private final PrivateMessageMapper messageMapper;
    private final SysUserMapper userMapper;
    private final ChatWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    /**
     * 发送私信消息
     *
     * @param senderId   发送者用户ID
     * @param receiverId 接收者用户ID
     * @param content    消息内容
     * @return 保存后的消息对象(含ID和createTime)
     */
    @Transactional
    public PrivateMessage sendMessage(Long senderId, Long receiverId, String content) {
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("不能给自己发送私信");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("消息内容不能为空");
        }
        if (content.length() > 500) {
            throw new RuntimeException("消息内容不能超过500个字符");
        }

        // 校验接收者是否存在且未禁用
        SysUser receiver = userMapper.selectById(receiverId);
        if (receiver == null) {
            throw new RuntimeException("接收者不存在");
        }
        if (receiver.getStatus() != null && receiver.getStatus() == 0) {
            throw new RuntimeException("接收者账号已被禁用");
        }

        // 1. 入库
        PrivateMessage message = new PrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content.trim());
        message.setIsRead(0);
        messageMapper.insert(message);

        // 重新查询以获取联表的用户信息(昵称/头像)用于推送
        List<PrivateMessage> list = messageMapper.selectConversation(senderId, receiverId);
        PrivateMessage saved = null;
        for (PrivateMessage m : list) {
            if (m.getId().equals(message.getId())) {
                saved = m;
                break;
            }
        }
        if (saved == null) {
            saved = message;
        }

        // 2. 通过WebSocket推送至接收者(所有在线设备)
        pushMessageToUser(receiverId, saved, "PRIVATE_MESSAGE");

        // 3. 同时给发送者本身推送一份(用于多设备同步:发送者在另一设备也能看到)
        pushMessageToUser(senderId, saved, "MESSAGE_SENT");

        log.info("私信已发送: from={} to={} msgId={}", senderId, receiverId, saved.getId());
        return saved;
    }

    /**
     * 获取两个用户之间的历史消息
     */
    public List<PrivateMessage> getConversation(Long userId1, Long userId2) {
        return messageMapper.selectConversation(userId1, userId2);
    }

    /**
     * 获取用户的会话列表(每个对话伙伴的最新一条消息)
     */
    public List<PrivateMessage> getConversations(Long userId) {
        return messageMapper.selectConversations(userId);
    }

    /**
     * 获取用户未读消息总数
     */
    public int countUnread(Long userId) {
        return messageMapper.countUnread(userId);
    }

    /**
     * 标记与某用户的会话为已读(当前用户作为接收者)
     */
    @Transactional
    public int markConversationRead(Long currentUserId, Long otherUserId) {
        return messageMapper.markConversationRead(currentUserId, otherUserId);
    }

    /**
     * 推送消息到指定用户
     *
     * @param userId  目标用户ID
     * @param message 消息对象
     * @param type    消息类型(PRIVATE_MESSAGE=接收新消息, MESSAGE_SENT=自己发送的消息同步)
     */
    private void pushMessageToUser(Long userId, PrivateMessage message, String type) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", type);
            payload.put("id", message.getId());
            payload.put("senderId", message.getSenderId());
            payload.put("receiverId", message.getReceiverId());
            payload.put("content", message.getContent());
            payload.put("createTime", message.getCreateTime());
            payload.put("senderUsername", message.getSenderUsername());
            payload.put("senderNickname", message.getSenderNickname());
            payload.put("senderAvatar", message.getSenderAvatar());
            payload.put("receiverUsername", message.getReceiverUsername());
            payload.put("receiverNickname", message.getReceiverNickname());
            payload.put("receiverAvatar", message.getReceiverAvatar());

            String json = objectMapper.writeValueAsString(payload);
            boolean delivered = webSocketHandler.sendToUser(userId, json);
            if (!delivered) {
                log.debug("用户{}不在线,消息仅入库不推送", userId);
            }
        } catch (Exception e) {
            log.error("推送消息失败: userId={}, msgId={}, error={}",
                    userId, message.getId(), e.getMessage());
        }
    }
}
