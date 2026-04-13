package com.daily.dailychineseculture.dto;

import java.util.Date;

/**
 * 群聊DTO
 */
public class GroupChatDTO {
    private Integer chatId;
    private String name;
    private String type;
    private String content;
    private Integer campId;
    private Integer classId;
    private Integer bigGroupId;
    private Integer smallGroupId;
    private Date createTime;
    private Integer unreadCount;

    // getters and setters
    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
}