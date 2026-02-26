package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 登录结果DTO
 * 包含token、信息完整性状态和用户基本信息
 */
@Data
public class LoginResult {
    /**
     * 访问令牌
     */
    private String token;
    
    /**
     * 用户信息是否完整
     * true: 信息已完善，无需跳转补全页
     * false: 信息不完整，需要跳转到补全页面
     */
    private Boolean isComplete;
    
    /**
     * 用户基本信息
     */
    private UserInfoDTO userInfo;
}