package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.DutyApplicationSubmitDTO;
import com.daily.dailychineseculture.dto.RevokeApplicationDTO;
import com.daily.dailychineseculture.vo.DutyApplicationVO;

import java.util.List;

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

    /**
     * 获取我的申请列表
     *
     * @param userId 当前登录用户ID
     * @return 申请列表
     */
    List<DutyApplicationVO> getMyApplicationList(Long userId);

    /**
     * 撤销申请
     * 包含防越权校验和状态流转校验
     *
     * @param userId 当前登录用户ID
     * @param dto    撤销请求DTO
     */
    void revokeApplication(Long userId, RevokeApplicationDTO dto);
}
