package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.vo.AdminDutyApplicationListItemVO;
import com.daily.dailychineseculture.vo.AdminDutyApplicationStatsVO;
import com.github.pagehelper.PageInfo;

/**
 * 管理端权限申请服务接口
 */
public interface AdminDutyApplicationService {

    /**
     * 获取审批统计数据
     * 实现数据隔离：非超级管理员只能看到自己角色权限范围内的申请
     *
     * @param currentRole 当前登录管理员角色
     * @return 统计数据
     */
    AdminDutyApplicationStatsVO getStats(String currentRole);

    /**
     * 分页查询审批列表
     * 实现数据隔离：非超级管理员只能看到自己角色权限范围内的申请
     *
     * @param currentRole 当前登录管理员角色
     * @param page        页码
     * @param size        每页条数
     * @param status      状态过滤（可选）
     * @param dutyType    权限类型过滤（可选，超级管理员使用）
     * @return 分页列表
     */
    PageInfo<AdminDutyApplicationListItemVO> getApplicationList(
            String currentRole,
            Integer page,
            Integer size,
            Integer status,
            String dutyType);
}
