package com.daily.dailychineseculture.entity;

import java.util.Date;

/**
 * 群聊成员实体类
 */
public class GroupChatMember {
    private Integer id; // 主键
    private Integer chatId; // 群聊ID
    private Long userId; // 用户ID
    private String role; // 角色: 管理员/成员
    private Date joinTime; // 加入时间

    // Getter和Setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Date getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Date joinTime) {
        this.joinTime = joinTime;
    }
}