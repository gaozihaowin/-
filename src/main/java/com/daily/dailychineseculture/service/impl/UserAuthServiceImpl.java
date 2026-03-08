package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.SwitchIdentityRequest;
import com.daily.dailychineseculture.dto.UserCurrentInfoDTO;
import com.daily.dailychineseculture.dto.UserIdentityDTO;
import com.daily.dailychineseculture.entity.User;
import com.daily.dailychineseculture.mapper.DutyAssignmentMapper;
import com.daily.dailychineseculture.mapper.UserMapper;
import com.daily.dailychineseculture.service.UserAuthService;
import com.daily.dailychineseculture.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户认证服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {
    
    private final UserMapper userMapper;
    private final DutyAssignmentMapper dutyAssignmentMapper;
    private final JwtUtils jwtUtils;
    
    @Override
    public UserCurrentInfoDTO getCurrentUserInfo(Long userId) {
        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        // 从当前 Token 上下文中获取当前职责类型（由拦截器注入）
        String currentDuty = getCurrentDutyFromContext();
        
        // 昵称优先使用 nickname，为空则使用 account
        String nickname = user.getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = user.getAccount();
        }
        
        // 转换职责名称
        String currentDutyName = convertDutyTypeToName(currentDuty);
        
        // Mock 未读通知数量（未来可从数据库查询）
        Integer unreadNoticeCount = 3;
        
        return new UserCurrentInfoDTO(
            user.getUserId(),
            nickname,
            user.getAvatar() != null ? user.getAvatar() : "",
            currentDuty,
            currentDutyName,
            unreadNoticeCount
        );
    }
    
    @Override
    public List<UserIdentityDTO> getUserIdentities(Long userId) {
        // 查询用户的所有有效任命记录（包含已过期和未过期的）
        List<Map<String, Object>> assignments = dutyAssignmentMapper.selectByUserId(userId);
        
        return assignments.stream()
            .map(this::convertToUserIdentityDTO)
            .toList();
    }
    
    @Override
    public String switchIdentity(Long userId, SwitchIdentityRequest request) {
        // 默认使用 ADMIN 类型（PC 管理端）
        return executeIdentitySwitch(userId, request, "ADMIN");
    }
    
    @Override
    public String executeIdentitySwitch(Long userId, SwitchIdentityRequest request, String clientType) {
        // 1. 校验 assignmentId 是否属于该用户
        Map<String, Object> assignment = dutyAssignmentMapper.selectById(request.getAssignmentId());
        if (assignment == null) {
            throw new IllegalArgumentException("任命记录不存在");
        }
        
        Long assignmentUserId = (Long) assignment.get("user_id");
        if (assignmentUserId == null || !assignmentUserId.equals(userId)) {
            throw new IllegalArgumentException("无权切换到该身份");
        }
        
        // 2. 获取新的 dutyType 和 campId
        String newDutyType = (String) assignment.get("duty_type");
        Integer campId = (Integer) assignment.get("camp_id");
        
        // 3. 根据客户端类型构建不同的 Token 载荷
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("dutyType", newDutyType);
        
        if (campId != null) {
            claims.put("campId", campId);
        }
        
        // 4. 添加客户端类型标记（用于区分 PC 端和小程序端）
        claims.put("clientType", clientType);
        
        // 5. PC 管理端 Token 可以包含额外的管理员权限标记
        if ("ADMIN".equals(clientType)) {
            claims.put("isAdmin", true);
            // TODO: 可以在这里添加更细粒度的权限列表
            // claims.put("permissions", Arrays.asList("USER_MANAGE", "CAMP_MANAGE", ...));
        }
        
        // 6. 生成 JWT Token
        return jwtUtils.generateToken(claims);
    }
    
    /**
     * 从当前上下文获取职责类型
     * TODO: 实际应从 ThreadLocal 或 RequestAttribute 中获取
     */
    private String getCurrentDutyFromContext() {
        // 临时实现：返回默认值
        // 实际应由 AuthInterceptor 从 Token 解析并存入 RequestAttribute
        return "COURSE_ADMIN";
    }
    
    /**
     * 转换职责类型为中文名称
     */
    private String convertDutyTypeToName(String dutyType) {
        if (dutyType == null) {
            return "课程管理员";
        }
        
        return switch (dutyType) {
            case "COURSE_ADMIN" -> "课程管理员";
            case "学班" -> "学班";
            case "志愿者" -> "志愿者";
            default -> dutyType;
        };
    }
    
    /**
     * 将任命记录转换为 UserIdentityDTO
     */
    private UserIdentityDTO convertToUserIdentityDTO(Map<String, Object> assignment) {
        Integer assignmentId = (Integer) assignment.get("assignment_id");
        String dutyType = (String) assignment.get("duty_type");
        Integer campId = (Integer) assignment.get("camp_id");
        String campName = (String) assignment.get("camp_name");
        
        // 如果 campId 为空，则营期名称为 "全局教务"
        if (campId == null) {
            campName = "全局教务";
        }
        
        return UserIdentityDTO.builder()
            .assignmentId(assignmentId)
            .dutyType(dutyType)
            .dutyName(dutyType) // 简化处理，直接用 dutyType 作为显示名
            .campId(campId)
            .campName(campName)
            .build();
    }
}
