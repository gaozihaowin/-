package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 管理员登录请求 DTO
 */
@Data
public class AdminLoginRequest {
    
    /**
     * 账号
     */
    private String account;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 登录角色类型
     * COURSE_ADMIN(课程管理), ARCHIVE_ADMIN(档案管理), SUPER_ADMIN(总管理员)
     */
    private String loginRole;
}
