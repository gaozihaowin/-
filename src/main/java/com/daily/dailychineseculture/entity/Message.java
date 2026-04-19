package com.daily.dailychineseculture.entity;

import java.util.Date;

/**
 * 消息实体类
 */
public class Message {
    private Long messageId; // 消息ID
    private Integer chatId; // 群聊ID
    private Long senderId; // 发送者ID
    private String content; // 消息内容
    private Date sendTime; // 发送时间
    private String recipientType; // 接收者类型: 所有人/特定人
    private Long recipientId; // 接收者ID(当recipient_type为特定人时)
    private Integer status; // 消息状态: 0-未读, 1-已读

    // Getter和Setter方法
    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}