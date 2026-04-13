package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.DutyApplicationSubmitDTO;
import com.daily.dailychineseculture.dto.RevokeApplicationDTO;
import com.daily.dailychineseculture.entity.DutyApplication;
import com.daily.dailychineseculture.entity.DutyAssignment;
import com.daily.dailychineseculture.mapper.DutyApplicationMapper;
import com.daily.dailychineseculture.mapper.DutyAssignmentMapper;
import com.daily.dailychineseculture.service.DutyApplicationService;
import com.daily.dailychineseculture.vo.DutyApplicationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<DutyApplicationVO> getMyApplicationList(Long userId) {
        // 查询当前用户的所有申请记录（按创建时间倒序）
        List<DutyApplication> applications = dutyApplicationMapper.selectByUserId(userId);

        // 转换为 VO 返回
        return applications.stream().map(app -> {
            DutyApplicationVO vo = new DutyApplicationVO();
            vo.setApplyId(app.getApplyId());
            vo.setDutyType(app.getDutyType());
            vo.setApplyReason(app.getApplyReason());
            vo.setStatus(app.getStatus());
            vo.setAuditRemark(app.getAuditRemark());
            vo.setCreateTime(app.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void revokeApplication(Long userId, RevokeApplicationDTO dto) {
        Integer applyId = dto.getApplyId();

        // ========== 查询申请记录 ==========
        DutyApplication app = dutyApplicationMapper.selectById(applyId);
        
        // 防呆：申请记录不存在
        if (app == null) {
            throw new RuntimeException("申请记录不存在");
        }

        // ========== 防越权校验 ==========
        // 核心安全：必须校验申请归属，防止越权操作他人记录
        if (!app.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作他人的申请记录");
        }

        // ========== 状态流转校验 ==========
        // 仅待审核状态（status = 0）可以撤销
        if (app.getStatus() != 0) {
            throw new RuntimeException("当前状态无法撤销，仅待审核状态可撤回");
        }

        // ========== 执行撤销 ==========
        DutyApplication updateEntity = new DutyApplication();
        updateEntity.setApplyId(applyId);
        updateEntity.setStatus(3);  // 3 = 已撤销

        dutyApplicationMapper.updateStatus(updateEntity);
    }
}
