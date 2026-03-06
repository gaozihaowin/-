package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 管理员登录响应 DTO
 */
@Data
public class AdminLoginResult {
    
    /**
     * JWT Token
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
         * 用户 ID
         */
        private String userId;
        
        /**
         * 账号
         */
        private String account;
        
        /**
         * 昵称
         */
        private String nickname;
        
        /**
         * 当前角色
         */
        private String currentRole;
        
        /**
         * 营期 ID（志愿者有具体值，管理员为 null）
         */
        private Integer campId;
    }
}
