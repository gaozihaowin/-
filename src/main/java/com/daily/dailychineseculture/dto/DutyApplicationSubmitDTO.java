package com.daily.dailychineseculture.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 权限申请提交DTO
 */
@Data
public class DutyApplicationSubmitDTO {

    /**
     * 权限类型（必填）
     * 可选值：COURSE_ADMIN(课程管理员), ARCHIVE_ADMIN(档案管理员), SUPER_ADMIN(超级管理员)
     */
    @NotBlank(message = "权限类型不能为空")
    private String dutyType;

    /**
     * 申请理由（必填）
     */
    @NotBlank(message = "申请理由不能为空")
    private String applyReason;
}
