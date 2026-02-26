package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 用户信息DTO
 * 用于登录接口返回完整的用户基本信息
 */
@Data
public class UserInfoDTO {
    /**
     * 用户ID
     */
    private String userid;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 性别: 0未知, 1男, 2女
     */
    private Integer gender;
    
    /**
     * 生日
     */
    private String birthday;
}