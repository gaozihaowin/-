package com.daily.dailychineseculture.dto;

/**
 * 群聊请求DTO
 */
public class GroupChatRequestDTO {
    private Integer campId;
    private Integer classId;
    private String className;
    private Integer bigGroupId;
    private String bigGroupName;
    private Integer smallGroupId;
    private String smallGroupName;
    private Integer targetId;
    private String dutyType;
    private Integer chatId;
    private Long userId;
    private String role;
    private String content;
    private String recipientType;
    private Long recipientId;
    private Integer page;
    private Integer size;
    private Integer messageId;

    // getters and setters
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getBigGroupId() {
        return bigGroupId;
    }

    public void setBigGroupId(Integer bigGroupId) {
        this.bigGroupId = bigGroupId;
    }

    public String getBigGroupName() {
        return bigGroupName;
    }

    public void setBigGroupName(String bigGroupName) {
        this.bigGroupName = bigGroupName;
    }

    public Integer getSmallGroupId() {
        return smallGroupId;
    }

    public void setSmallGroupId(Integer smallGroupId) {
        this.smallGroupId = smallGroupId;
    }

    public String getSmallGroupName() {
        return smallGroupName;
    }

    public void setSmallGroupName(String smallGroupName) {
        this.smallGroupName = smallGroupName;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public String getDutyType() {
        return dutyType;
    }

    public void setDutyType(String dutyType) {
        this.dutyType = dutyType;
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

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }
}