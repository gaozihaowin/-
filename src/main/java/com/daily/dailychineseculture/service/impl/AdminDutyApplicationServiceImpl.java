package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.mapper.AdminDutyApplicationMapper;
import com.daily.dailychineseculture.service.AdminDutyApplicationService;
import com.daily.dailychineseculture.vo.AdminDutyApplicationListItemVO;
import com.daily.dailychineseculture.vo.AdminDutyApplicationStatsVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端权限申请服务实现类
 * 核心职责：实现基于角色的数据隔离
 */
@Service
public class AdminDutyApplicationServiceImpl implements AdminDutyApplicationService {

    @Autowired
    private AdminDutyApplicationMapper adminDutyApplicationMapper;

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
}
