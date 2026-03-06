package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.AdminLoginRequest;
import com.daily.dailychineseculture.dto.AdminLoginResult;

/**
 * 管理员认证服务接口
 */
public interface AdminAuthService {
    
    /**
     * 管理员登录
     * 
     * @param request 登录请求参数
     * @return 登录结果（包含 Token 和用户信息）
     */
    AdminLoginResult adminLogin(AdminLoginRequest request);
}
