package com.daily.dailychineseculture.dto;

public class GroupChatRequestDTO {
    private Integer campId;
    private Integer classId;
    private Integer bigGroupId;
    private Integer smallGroupId;
    private String className;
    private String bigGroupName;
    private String smallGroupName;
    private Integer chatId;
    private Long userId;
    private String role;
    private String content;
    private String messageType;
    private String voiceUrl;
    private Integer voiceDuration;
    private String recipientType;
    private Long recipientId;
    private String dutyType;
    private Integer targetId;
    private Integer quotedMessageId;
    private Integer messageId;

    // Getters and Setters
    public Integer getCampId() {
        return campId;
    }

    public void setCampId(Integer campId) {
        this.campId = campId;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public Integer getBigGroupId() {
        return bigGroupId;
    }

    public void setBigGroupId(Integer bigGroupId) {
        this.bigGroupId = bigGroupId;
    }

    public Integer getSmallGroupId() {
        return smallGroupId;
    }

    public void setSmallGroupId(Integer smallGroupId) {
        this.smallGroupId = smallGroupId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getBigGroupName() {
        return bigGroupName;
    }

    public void setBigGroupName(String bigGroupName) {
        this.bigGroupName = bigGroupName;
    }

    public String getSmallGroupName() {
        return smallGroupName;
    }

    public void setSmallGroupName(String smallGroupName) {
        this.smallGroupName = smallGroupName;
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    public Integer getVoiceDuration() {
        return voiceDuration;
    }

    public void setVoiceDuration(Integer voiceDuration) {
        this.voiceDuration = voiceDuration;
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

    public String getDutyType() {
        return dutyType;
    }

    public void setDutyType(String dutyType) {
        this.dutyType = dutyType;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public Integer getQuotedMessageId() {
        return quotedMessageId;
    }

    public void setQuotedMessageId(Integer quotedMessageId) {
        this.quotedMessageId = quotedMessageId;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }
}