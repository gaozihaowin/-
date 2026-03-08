package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.SwitchIdentityRequest;
import com.daily.dailychineseculture.dto.UserCurrentInfoDTO;
import com.daily.dailychineseculture.dto.UserIdentityDTO;

import java.util.List;

/**
 * 用户认证服务接口
 */
public interface UserAuthService {
    
    /**
     * 获取当前登录用户的状态信息
     * 
     * @param userId 用户 ID（从 Token 中解析）
     * @return 用户当前信息
     */
    UserCurrentInfoDTO getCurrentUserInfo(Long userId);
    
    /**
     * 获取用户可切换的身份列表
     * 
     * @param userId 用户 ID（从 Token 中解析）
     * @return 身份信息列表
     */
    List<UserIdentityDTO> getUserIdentities(Long userId);
    
    /**
     * 执行身份切换
     * 
     * @param userId 用户 ID（从 Token 中解析）
     * @param request 切换请求
     * @return 新的 JWT Token
     */
    String switchIdentity(Long userId, SwitchIdentityRequest request);
    
    /**
     * 执行身份切换（支持多端差异化处理）
     * 
     * @param userId 用户 ID
     * @param request 切换请求
     * @param clientType 客户端类型（"ADMIN" = PC 管理端，"APP" = 小程序端）
     * @return 新的 JWT Token
     */
    String executeIdentitySwitch(Long userId, SwitchIdentityRequest request, String clientType);
}
