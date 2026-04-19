package com.daily.dailychineseculture.websocket;

import com.daily.dailychineseculture.dto.GroupChatDTO;
import com.daily.dailychineseculture.dto.MessageDTO;
import com.daily.dailychineseculture.mapper.GroupChatMapper;
import com.daily.dailychineseculture.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GroupChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private GroupChatMapper groupChatMapper;

    @Autowired
    private JwtUtils jwtUtils;

    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private static final Map<Integer, Set<Long>> groupMembers = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket连接已建立: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            System.out.println("收到WebSocket消息: " + payload);

            if (payload == null || payload.isEmpty() || "undefined".equals(payload) || "null".equals(payload)) {
                System.out.println("无效的消息内容: " + payload);
                return;
            }

            Map<String, Object> messageData;
            try {
                messageData = objectMapper.readValue(payload, Map.class);
            } catch (Exception e) {
                System.out.println("收到纯文本消息: " + payload);
                return;
            }

            String type = (String) messageData.get("type");
            System.out.println("消息类型: " + type);

            switch (type) {
                case "auth":
                    handleAuthMessage(session, messageData);
                    break;
                case "send":
                    Long userId = messageData.get("userId") != null
                            ? ((Number) messageData.get("userId")).longValue()
                            : null;
                    handleSendMessage(messageData, userId);
                    break;
                case "markRead":
                    Long readUserId = messageData.get("userId") != null
                            ? ((Number) messageData.get("userId")).longValue()
                            : null;
                    handleMarkRead(messageData, readUserId);
                    break;
                case "joinGroup":
                    Long joinUserId = messageData.get("userId") != null
                            ? ((Number) messageData.get("userId")).longValue()
                            : null;
                    handleJoinGroup(messageData, joinUserId);
                    break;
                case "leaveGroup":
                    Long leaveUserId = messageData.get("userId") != null
                            ? ((Number) messageData.get("userId")).longValue()
                            : null;
                    handleLeaveGroup(messageData, leaveUserId);
                    break;
                case "revoke":
                    Long revokeUserId = messageData.get("userId") != null
                            ? ((Number) messageData.get("userId")).longValue()
                            : null;
                    Integer messageId = messageData.get("messageId") != null
                            ? ((Number) messageData.get("messageId")).intValue()
                            : null;
                    if (messageId != null && revokeUserId != null) {
                        broadcastRevoke(messageId, revokeUserId);
                    }
                    break;
                default:
                    System.out.println("未知消息类型: " + type);
                    break;
            }
        } catch (Exception e) {
            System.out.println("处理WebSocket消息出错:");
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = null;
        for (Map.Entry<Long, WebSocketSession> entry : userSessions.entrySet()) {
            if (entry.getValue().equals(session)) {
                userId = entry.getKey();
                break;
            }
        }
        if (userId != null) {
            userSessions.remove(userId);
            for (Map.Entry<Integer, Set<Long>> entry : groupMembers.entrySet()) {
                entry.getValue().remove(userId);
            }
        }
    }

    private void handleSendMessage(Map<String, Object> messageData, Long userId) throws Exception {
        Integer chatId = (Integer) messageData.get("chatId");
        String content = (String) messageData.get("content");
        System.out.println("接收到的消息内容: " + content);
        String messageType = (String) messageData.get("messageType");
        if (messageType == null) messageType = "text";

        String voiceUrl = (String) messageData.get("voiceUrl");
        Integer voiceDuration = messageData.get("voiceDuration") != null
                ? ((Number) messageData.get("voiceDuration")).intValue()
                : null;
        String recipientType = (String) messageData.get("recipientType");
        Long recipientId = messageData.get("recipientId") != null
                ? ((Number) messageData.get("recipientId")).longValue()
                : null;

        String realType = "all".equals(recipientType) ? "group" : "private";
        Long recv = "group".equals(realType) ? null : recipientId;
        groupChatMapper.sendMessage(chatId, userId, recv, content, messageType, voiceUrl, voiceDuration);

        List<MessageDTO> messages = groupChatMapper.getGroupMessages(chatId, userId, 1, 0);
        if (!messages.isEmpty()) {
            MessageDTO message = messages.get(0);
            Map<String, Object> response = new HashMap<>();
            response.put("type", "message");
            response.put("message", message);
            if ("private".equals(realType) && recipientId != null) {
                // 私聊消息：只发送给发送者和接收者
                try {
                    // 发送给接收者
                    sendToUser(recipientId, response);
                    // 发送给发送者自己
                    sendToUser(userId, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // 群聊消息：广播给所有成员
                broadcastMessage(chatId, response);
            }
        }
    }

    private void handleMarkRead(Map<String, Object> messageData, Long userId) {
        Integer messageId = (Integer) messageData.get("messageId");
        Integer chatId = (Integer) messageData.get("chatId");

        if (messageId != null) {
            groupChatMapper.markMessageAsRead(messageId, userId);
        } else if (chatId != null) {
            groupChatMapper.markGroupMessageAsRead(chatId, userId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("type", "read");
        response.put("userId", userId);
        response.put("chatId", chatId);
        response.put("messageId", messageId);

        try {
            broadcastMessage(chatId, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleJoinGroup(Map<String, Object> messageData, Long userId) {
        Integer chatId = (Integer) messageData.get("chatId");
        if (chatId != null) {
            groupMembers.computeIfAbsent(chatId, k -> new HashSet<>()).add(userId);
        }
    }

    private void handleLeaveGroup(Map<String, Object> messageData, Long userId) {
        Integer chatId = (Integer) messageData.get("chatId");
        if (chatId != null) {
            Set<Long> members = groupMembers.get(chatId);
            if (members != null) members.remove(userId);
        }
    }

    // 处理认证消息
    private void handleAuthMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        String token = (String) messageData.get("token");
        Integer chatId = null;

        // 处理chatId，支持String和Number类型
        Object chatIdObj = messageData.get("chatId");
        if (chatIdObj != null) {
            if (chatIdObj instanceof Number) {
                chatId = ((Number) chatIdObj).intValue();
            } else if (chatIdObj instanceof String) {
                try {
                    chatId = Integer.parseInt((String) chatIdObj);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("收到认证消息，token: " + token + ", chatId: " + chatId);

        if (token != null) {
            Long userId = jwtUtils.getUserIdFromToken(token);
            System.out.println("解析用户ID: " + userId);

            if (userId != null) {
                // 存储用户会话
                userSessions.put(userId, session);
                System.out.println("存储用户会话: " + userId);

                // 加载用户所在的群聊
                List<GroupChatDTO> groups = groupChatMapper.getUserGroupChats(userId, 999, 0);
                System.out.println("用户所在群聊: " + groups);

                for (GroupChatDTO group : groups) {
                    Integer groupChatId = group.getChatId();
                    groupMembers.computeIfAbsent(groupChatId, k -> new HashSet<>()).add(userId);
                    System.out.println("添加用户到群聊: " + userId + " -> " + groupChatId);
                }

                // 如果指定了chatId，确保用户在该群聊中
                if (chatId != null) {
                    groupMembers.computeIfAbsent(chatId, k -> new HashSet<>()).add(userId);
                    System.out.println("添加用户到指定群聊: " + userId + " -> " + chatId);
                }

                // 发送认证成功响应
                Map<String, Object> response = new HashMap<>();
                response.put("type", "authSuccess");
                response.put("userId", userId);

                String jsonMessage = objectMapper.writeValueAsString(response);
                session.sendMessage(new TextMessage(jsonMessage));

                System.out.println("用户认证成功: " + userId);
                System.out.println("当前用户会话数: " + userSessions.size());
                System.out.println("当前群聊成员数: " + groupMembers.size());
            }
        }
    }

    public void broadcastRevoke(Integer messageId, Long userId) {
        MessageDTO msg = groupChatMapper.getMessageById(messageId);
        if (msg == null || !msg.getSenderId().equals(userId)) return;

        // 数据库标记撤回
        groupChatMapper.revokeMessage(messageId, userId);

        Map<String, Object> data = new HashMap<>();
        data.put("type", "message_revoke");
        data.put("messageId", messageId);
        data.put("chatId", msg.getChatId());

        try {
            if (msg.getReceiverId() == null) {
                // 群聊
                broadcastMessage(msg.getChatId(), data);
            } else {
                // 私聊
                sendToUser(userId, data);
                sendToUser(msg.getReceiverId(), data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(Integer chatId, Map<String, Object> message) throws Exception {
        System.out.println("开始广播消息，chatId: " + chatId);
        String jsonMessage = objectMapper.writeValueAsString(message);
        TextMessage textMessage = new TextMessage(jsonMessage);

        Set<Long> members = groupMembers.get(chatId);
        System.out.println("群聊成员列表: " + members);
        System.out.println("用户会话列表: " + userSessions.keySet());

        if (members != null) {
            System.out.println("群聊成员数量: " + members.size());
            for (Long memberId : members) {
                System.out.println("尝试发送消息给群聊成员: " + memberId);
                WebSocketSession session = userSessions.get(memberId);
                if (session != null && session.isOpen()) {
                    try {
                        System.out.println("发送消息给群聊成员: " + memberId);
                        session.sendMessage(textMessage);
                        System.out.println("消息发送成功: " + memberId);
                    } catch (IOException e) {
                        System.out.println("消息发送失败: " + memberId);
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("用户会话不存在或已关闭: " + memberId);
                }
            }
        } else {
            System.out.println("群聊成员列表为空: " + chatId);
        }
    }

    public void sendToUser(Long userId, Map<String, Object> message) throws Exception {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        }
    }

    public void broadcastToAll(Map<String, Object> message) throws Exception {
        String jsonMessage = objectMapper.writeValueAsString(message);
        TextMessage textMessage = new TextMessage(jsonMessage);
        for (WebSocketSession session : userSessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException ignored) {}
            }
        }
    }
}