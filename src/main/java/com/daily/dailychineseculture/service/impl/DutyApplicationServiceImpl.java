package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.DutyApplicationSubmitDTO;
import com.daily.dailychineseculture.entity.DutyApplication;
import com.daily.dailychineseculture.entity.DutyAssignment;
import com.daily.dailychineseculture.mapper.DutyApplicationMapper;
import com.daily.dailychineseculture.mapper.DutyAssignmentMapper;
import com.daily.dailychineseculture.service.DutyApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 权限申请服务实现类
 */
@Service
public class DutyApplicationServiceImpl implements DutyApplicationService {

    @Autowired
    private DutyApplicationMapper dutyApplicationMapper;

    @Autowired
    private DutyAssignmentMapper dutyAssignmentMapper;

    @Override
    public Integer submitApplication(Long userId, DutyApplicationSubmitDTO dto) {
        String dutyType = dto.getDutyType();

        // ========== 拦截一：防重复申请 ==========
        // 检查是否已有待审核的同类申请（status = 0 表示待审核）
        DutyApplication pendingApplication = dutyApplicationMapper.selectPendingApplication(userId, dutyType);
        if (pendingApplication != null) {
            throw new RuntimeException("您有待审核的同类申请，请勿重复提交");
        }

        // ========== 拦截二：防重复授权 ==========
        // 检查用户是否已拥有该权限（所有权限均为全局权限，不绑定营期）
        DutyAssignment existingAssignment = dutyAssignmentMapper.selectByUserIdAndDutyType(userId, dutyType);
        if (existingAssignment != null) {
            throw new RuntimeException("您已拥有该权限，无需重复申请");
        }

        // ========== 执行落库 ==========
        // 双重校验皆通过后，构建实体并存入数据库
        DutyApplication application = new DutyApplication();
        application.setUserId(userId);
        application.setDutyType(dutyType);
        application.setApplyReason(dto.getApplyReason());
        application.setStatus(0);  // 强制设置为待审核状态

        dutyApplicationMapper.insert(application);

        return application.getApplyId();
    }
}
