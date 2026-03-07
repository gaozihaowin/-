package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.AdminLoginRequest;
import com.daily.dailychineseculture.dto.AdminLoginResult;
import com.daily.dailychineseculture.entity.DutyAssignment;
import com.daily.dailychineseculture.entity.User;
import com.daily.dailychineseculture.mapper.DutyAssignmentMapper;
import com.daily.dailychineseculture.mapper.UserMapper;
import com.daily.dailychineseculture.service.AdminAuthService;
import com.daily.dailychineseculture.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 管理员认证服务实现类
 */
@Service
public class AdminAuthServiceImpl implements AdminAuthService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private DutyAssignmentMapper dutyAssignmentMapper;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * 管理员登录
     * 
     * @param request 登录请求参数
     * @return 登录结果（包含 Token 和用户信息）
     */
    @Override
    public AdminLoginResult adminLogin(AdminLoginRequest request) {
        // 1. 参数校验
        if (request.getAccount() == null || request.getAccount().trim().isEmpty()) {
            throw new IllegalArgumentException("账号不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (request.getLoginRole() == null || request.getLoginRole().trim().isEmpty()) {
            throw new IllegalArgumentException("登录角色不能为空");
        }
        
        // 2. 根据账号查询用户
        User user = userMapper.selectByAccount(request.getAccount());
        if (user == null) {
            // 账号不存在，返回特定错误
            throw new RuntimeException("账号或密码错误");
        }
        
        // 3. 校验密码
        if (!request.getPassword().equals(user.getPassword())) {
            // 密码错误
            throw new RuntimeException("账号或密码错误");
        }
        
        // 4. 查询用户的职位权限
        Map<String, Object> dutyInfo = dutyAssignmentMapper.selectWithCampInfo(
            user.getUserId(), 
            request.getLoginRole()
        );
        
        if (dutyInfo == null) {
            // 没有该角色权限 - 返回业务错误码 403
            throw new RuntimeException("无权以该身份登录:403");
        }
        
        // 6. 生成 JWT Token（包含 userId, currentRole, campId）
        Integer campId = (Integer) dutyInfo.get("camp_id");
        String token = jwtUtils.generateToken(
            user.getUserId(), 
            user.getAccount(), 
            request.getLoginRole(),
            campId
        );
        
        // 7. 组装返回结果
        AdminLoginResult result = new AdminLoginResult();
        result.setToken(token);
        
        AdminLoginResult.UserInfo userInfo = new AdminLoginResult.UserInfo();
        userInfo.setUserId(String.valueOf(user.getUserId()));
        userInfo.setAccount(user.getAccount());
        userInfo.setNickname(user.getNickname() != null ? user.getNickname() : user.getAccount());
        userInfo.setCurrentRole(request.getLoginRole());
        userInfo.setCampId(campId);
        
        result.setUserInfo(userInfo);
        
        return result;
    }
}
