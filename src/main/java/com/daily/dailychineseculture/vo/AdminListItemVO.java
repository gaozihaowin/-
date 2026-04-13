package com.daily.dailychineseculture.vo;

import lombok.Data;

import java.util.Date;

/**
 * 管理人员列表项VO
 */
@Data
public class AdminListItemVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 昵称（从用户表JOIN出来）
     */
    private String nickname;

    /**
     * 账户名（从用户表JOIN出来）
     */
    private String account;

    /**
     * 权限类型
     */
    private String dutyType;

    /**
     * 授权时间
     */
    private Date assignTime;
}
