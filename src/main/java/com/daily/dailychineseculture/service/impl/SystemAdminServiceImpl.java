package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.AssignRequest;
import com.daily.dailychineseculture.mapper.SystemAdminMapper;
import com.daily.dailychineseculture.service.SystemAdminService;
import com.daily.dailychineseculture.vo.AdminStatsVO;
import com.daily.dailychineseculture.vo.AdminUserAggVO;
import com.daily.dailychineseculture.vo.SystemAdminVO;
import com.daily.dailychineseculture.vo.UserSearchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemAdminServiceImpl implements SystemAdminService {

    private final SystemAdminMapper systemAdminMapper;

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
    public List<UserSearchVO> searchUsers(String keyword) {
        return systemAdminMapper.searchUsers(keyword);
    }

    @Override
    @Transactional
    public void assign(AssignRequest request) {
        int exists = systemAdminMapper.existsValidAssignment(request.getUserId(), request.getDutyType());
        if (exists > 0) {
            throw new RuntimeException("该用户已拥有此权限，无需重复授权");
        }
        systemAdminMapper.insertAssignment(request.getUserId(), request.getDutyType());
    }

    @Override
    @Transactional
    public void revoke(Integer assignmentId) {
        int rows = systemAdminMapper.deleteAssignment(assignmentId);
        if (rows == 0) {
            throw new RuntimeException("撤销失败，记录不存在或已失效");
        }
    }
}
