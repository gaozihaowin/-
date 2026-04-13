package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.DutyApplicationReviewDTO;
import com.daily.dailychineseculture.entity.DutyApplication;
import com.daily.dailychineseculture.entity.DutyAssignment;
import com.daily.dailychineseculture.mapper.AdminDutyApplicationMapper;
import com.daily.dailychineseculture.mapper.DutyApplicationMapper;
import com.daily.dailychineseculture.mapper.DutyAssignmentMapper;
import com.daily.dailychineseculture.service.AdminDutyApplicationService;
import com.daily.dailychineseculture.vo.AdminDutyApplicationListItemVO;
import com.daily.dailychineseculture.vo.AdminDutyApplicationStatsVO;
import com.daily.dailychineseculture.vo.AdminListItemVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理端权限申请服务实现类
 * 核心职责：实现基于角色的数据隔离
 */
@Service
public class AdminDutyApplicationServiceImpl implements AdminDutyApplicationService {

    @Autowired
    private AdminDutyApplicationMapper adminDutyApplicationMapper;

    @Autowired
    private DutyApplicationMapper dutyApplicationMapper;

    @Autowired
    private DutyAssignmentMapper dutyAssignmentMapper;

    /**
     * 超级管理员角色标识
     */
    private static final String SUPER_ADMIN = "SUPER_ADMIN";

    @Override
    public AdminDutyApplicationStatsVO getStats(String currentRole) {
        // ========== 数据隔离逻辑 ==========
        // SUPER_ADMIN 可以看到所有申请的统计
        // 其他角色只能看到与自己角色匹配的申请统计
        String dutyTypeFilter = null;
        if (!SUPER_ADMIN.equals(currentRole)) {
            // 非超级管理员：强制过滤为自己的角色类型
            dutyTypeFilter = currentRole;
        }

        return adminDutyApplicationMapper.selectStats(dutyTypeFilter);
    }

    @Override
    public PageInfo<AdminDutyApplicationListItemVO> getApplicationList(
            String currentRole,
            Integer page,
            Integer size,
            Integer status,
            String dutyType) {

        // ========== 数据隔离逻辑 ==========
        // SUPER_ADMIN 可以查看所有申请
        // 其他角色只能查看 duty_type 与自己角色匹配的申请
        String dutyTypeFilter = null;
        if (!SUPER_ADMIN.equals(currentRole)) {
            // 非超级管理员：强制过滤为自己的角色类型
            dutyTypeFilter = currentRole;
        }

        // ========== 分页查询 ==========
        // 使用 PageHelper 进行分页
        PageHelper.startPage(page, size);
        List<AdminDutyApplicationListItemVO> list = adminDutyApplicationMapper.selectApplicationList(
                dutyTypeFilter,
                status,
                dutyType
        );

        return new PageInfo<>(list);
    }

    /**
     * 审批流转（通过/拒绝）
     * 
     * 核心逻辑：
     * 1. 数据隔离校验 - 非SUPER_ADMIN只能审批自己角色类型的申请
     * 2. 状态防呆 - 已处理/已撤销的申请不能重复操作
     * 3. 审批写库 - 更新申请状态
     * 4. 发牌逻辑 - 通过时向授权表插入记录（防重复授权）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewApplication(Long reviewerId, String currentRole, DutyApplicationReviewDTO reviewDTO) {
        // ========== 步骤 1：数据获取与越权防呆 ==========
        Integer applyId = reviewDTO.getApplyId();
        DutyApplication application = dutyApplicationMapper.selectById(applyId);
        
        // 防呆：申请记录不存在
        if (application == null) {
            throw new RuntimeException("申请记录不存在");
        }
        
        // 数据隔离校验：非SUPER_ADMIN只能审批自己角色类型的申请
        if (!SUPER_ADMIN.equals(currentRole)) {
            String dutyType = application.getDutyType();
            if (!currentRole.equals(dutyType)) {
                throw new RuntimeException("越权操作：您无权审批该类型的权限申请");
            }
        }
        
        // ========== 步骤 2：状态防呆 ==========
        Integer currentStatus = application.getStatus();
        if (currentStatus == null || currentStatus != 0) {
            throw new RuntimeException("该申请已被处理或撤销，请勿重复操作");
        }
        
        // ========== 步骤 3：审批写库 ==========
        Integer newStatus = reviewDTO.getStatus();
        String auditRemark = reviewDTO.getAuditRemark();
        
        // 校验审批状态合法性
        if (newStatus == null || (newStatus != 1 && newStatus != 2)) {
            throw new RuntimeException("审批状态参数错误，只能为1(通过)或2(拒绝)");
        }
        
        // 拒绝时审核备注必填
        if (newStatus == 2 && (auditRemark == null || auditRemark.trim().isEmpty())) {
            throw new RuntimeException("拒绝时必须填写审核备注");
        }
        
        // 执行审批更新
        int updateRows = dutyApplicationMapper.updateForReview(
                applyId,
                newStatus,
                auditRemark,
                reviewerId
        );
        
        if (updateRows == 0) {
            throw new RuntimeException("审批更新失败，请重试");
        }
        
        // ========== 步骤 4：发牌逻辑（仅审批通过时执行）==========
        if (newStatus == 1) {
            Long applicantUserId = application.getUserId();
            String dutyType = application.getDutyType();
            
            // 防重复授权：先查询是否已存在
            DutyAssignment existingAssignment = dutyAssignmentMapper.selectByUserIdAndDutyTypeForReview(
                    applicantUserId, dutyType
            );
            
            if (existingAssignment != null) {
                // 已存在相同授权，跳过插入（幂等处理）
                // 不抛异常，因为这是合法的并发场景
            } else {
                // 插入新授权记录（全局权限，不再维护 camp_id）
                int insertRows = dutyAssignmentMapper.insertAssignment(
                        applicantUserId,
                        dutyType
                );
                
                if (insertRows == 0) {
                    throw new RuntimeException("授权记录插入失败，请重试");
                }
            }
        }
    }

    /**
     * 查询管理人员列表
     * 实现数据隔离：非超级管理员只能看到自己角色权限范围内的管理员
     *
     * @param currentRole 当前登录管理员角色
     * @return 管理人员列表
     */
    @Override
    public List<AdminListItemVO> getAdminList(String currentRole) {
        // ========== 数据隔离逻辑 ==========
        // SUPER_ADMIN 可以看到所有管理员
        // 其他角色只能看到与自己角色匹配的管理员
        String dutyTypeFilter = null;
        if (!SUPER_ADMIN.equals(currentRole)) {
            // 非超级管理员：强制过滤为自己的角色类型
            dutyTypeFilter = currentRole;
        }

        return dutyAssignmentMapper.selectAdminList(dutyTypeFilter);
    }
}
