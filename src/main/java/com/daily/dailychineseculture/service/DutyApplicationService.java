package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.DutyApplicationSubmitDTO;

/**
 * 权限申请服务接口
 */
public interface DutyApplicationService {

    /**
     * 提交权限申请
     * 实现双重防呆校验：
     * 1. 防重复申请：检查是否已有待审核的同类申请
     * 2. 防重复授权：检查是否已拥有该权限
     *
     * @param userId 当前登录用户ID
     * @param dto    申请提交DTO
     * @return 申请ID
     */
    Integer submitApplication(Long userId, DutyApplicationSubmitDTO dto);
}
