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

    /**
     * 获取用户的所有证书（不包括作业证书）
     */
    List<Map<String, Object>> getCertificatesByUser(Long userId);

    /**
     * 获取用户的所有证书（包括作业证书）
     */
    List<Map<String, Object>> getAllCertificatesByUser(Long userId);

    /**
     * 为志愿者颁发证书
     */
    boolean issueCertificate(Long volunteerId, String certificateType, Integer assignmentId, Long homeworkId);

    /**
     * 取消颁发证书
     */
    boolean cancelCertificate(Long volunteerId, String certificateType, Integer assignmentId, Long homeworkId);

    /**
     * 检查志愿者是否已颁发证书
     */
    boolean checkCertificateIssued(Long volunteerId,String certificateType, Integer assignmentId, Long homeworkId);

    /**
     * 根据作业ID获取证书列表
     */
    List<Map<String, Object>> getCertificatesByHomeworkId(Long homeworkId);

    /**
     * 获取志愿者详情
     */
    Map<String, Object> getVolunteerDetail(Long volunteerId);


    /**
     * 获取管理范围内的志愿者列表
     */
    List<Map<String, Object>> getManagedVolunteers(Long userId, Integer assignmentId);
    /**
     * 获取管理范围内的志愿者档案
     */
    List<Map<String, Object>> getUserAllAssignments(Long userId);
    /**
     * 检查用户是否为超级管理员
     */
    boolean checkAdminPermission(Long userId);

    /**
     * 获取正在进行的营期列表
     */
    List<Map<String, Object>> getActiveCamps();

    /**
     * 获取营期下的班级列表
     */
    List<Map<String, Object>> getClassesByCampId(Integer campId);

    /**
     * 获取营期下的班长列表
     */
    List<Map<String, Object>> getMonitorsByCampId(Integer campId);
}