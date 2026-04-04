package com.daily.dailychineseculture.entity;

import java.util.Date;

/**
 * 群聊实体类
 */
public class GroupChat {
    private Integer chatId; // 群聊ID
    private String name; // 群聊名称
    private String type; // 类型: 班级群/大组群/小组群
    private String content; // 群公告/简介
    private Date createTime; // 创建时间
    private Integer campId; // 营期ID
    private Integer classId; // 班级ID
    private Integer bigGroupId; // 大组ID
    private Integer smallGroupId; // 小组ID

    // Getter和Setter方法
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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
}