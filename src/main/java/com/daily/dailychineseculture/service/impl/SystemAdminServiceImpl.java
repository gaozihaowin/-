package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.AssignRequest;
import com.daily.dailychineseculture.dto.RevokeRequest;
import com.daily.dailychineseculture.entity.DutyAssignment;
import com.daily.dailychineseculture.mapper.AdminDutyApplicationMapper;
import com.daily.dailychineseculture.mapper.SystemAdminMapper;
import com.daily.dailychineseculture.mapper.UserMapper;
import com.daily.dailychineseculture.service.SystemAdminService;
import com.daily.dailychineseculture.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemAdminServiceImpl implements SystemAdminService {

    private final SystemAdminMapper systemAdminMapper;
    private final UserMapper userMapper;
    private final AdminDutyApplicationMapper adminDutyApplicationMapper;

    @Override
    public AdminStatsVO getAdminStats() {
        return systemAdminMapper.selectAdminStats();
    }

    @Override
    public List<SystemAdminVO> getAdminList(String keyword, Integer page, Integer pageSize) {
        Integer offset = (page != null && pageSize != null) ? (page - 1) * pageSize : null;
        Integer limit = pageSize;
        return systemAdminMapper.selectAdminList(keyword, offset, limit);
    }

    @Override
    public List<AdminUserAggVO> getAdminListAgg(String keyword) {
        return systemAdminMapper.selectAdminUserAggRows(keyword);
    }

    @Override
    public AdminUserDetailVO getAdminDetail(Long userId) {
        AdminUserDetailVO detail = new AdminUserDetailVO();
        detail.setUserInfo(userMapper.selectUserBaseInfo(userId));
        detail.setActiveRoles(systemAdminMapper.selectActiveRolesByUserId(userId));
        detail.setApplicationHistory(adminDutyApplicationMapper.selectApplicationHistoryByUserId(userId));
        return detail;
    }

    @Override
    public List<UserSearchVO> searchUsers(String keyword) {
        return systemAdminMapper.searchUsers(keyword);
    }

    @Override
    @Transactional
    public void assign(AssignRequest request, Long adminId) {
        int exists = systemAdminMapper.existsValidAssignment(request.getUserId(), request.getDutyType());
        if (exists > 0) {
            throw new RuntimeException("该用户已拥有此权限，无需重复授权");
        }
        systemAdminMapper.insertAssignment(request.getUserId(), request.getDutyType());
        Date now = new Date();
        adminDutyApplicationMapper.insertApplicationAudit(
                request.getUserId(),
                request.getDutyType(),
                "总管理员直接任命",
                1,
                adminId,
                now,
                request.getReason(),
                now
        );
    }

    @Override
    @Transactional
    public void revoke(RevokeRequest request, Long adminId) {
        DutyAssignment assignment = systemAdminMapper.selectAssignmentById(request.getAssignmentId());
        if (assignment == null) {
            throw new RuntimeException("撤销失败，记录不存在或已失效");
        }
        systemAdminMapper.deleteAssignment(request.getAssignmentId());
        Date now = new Date();
        adminDutyApplicationMapper.insertApplicationAudit(
                assignment.getUserId(),
                assignment.getDutyType(),
                "总管理员强制撤权",
                4,
                adminId,
                now,
                request.getReason(),
                now
        );
    }
}
