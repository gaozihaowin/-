package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 登录结果DTO
 */
@Data
public class LoginResult {
    /**
     * 访问令牌
     */
    private String token;
    
    /**
     * 用户信息
     */
    private UserInfo userInfo;
    
    /**
     * 用户信息内部类
     */
    @Data
    public static class UserInfo {
        /**
         * 用户姓名
         */
        private String name;
        
        /**
         * 用户头像URL
         */
        private String avatar;
    }
}