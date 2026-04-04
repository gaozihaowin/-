package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.MemberManageDTO;
import com.daily.dailychineseculture.dto.DutyAssignmentDTO;
import java.util.List;
import java.util.Map;

/**
 * 志愿者管理服务接口
 */
public interface VolunteerManageService {

    /**
     * 获取用户的所有管理范围
     */
    List<Map<String, Object>> getManagementScopes(Long userId);

    /**
     * 获取管理成员信息
     */
    MemberManageDTO getMemberManageInfo(Long userId, Integer assignmentId, Integer smallGroupId);

    /**
     * 获取分配岗位信息
     */
    DutyAssignmentDTO getDutyAssignmentInfo(Long userId, Integer assignmentId);

    /**
     * 分配岗位
     */
    boolean assignDuty(Long managerUserId, Long targetUserId, String targetType,
                       Integer targetId, String dutyType);

    /**
     * 移除岗位
     */
    boolean removeDuty(Long managerUserId, Integer assignmentId);
}