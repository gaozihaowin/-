package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 切换身份请求参数
 */
@Data
public class SwitchIdentityRequest {
    /**
     * 任命记录 ID（必填）
     */
    private Integer assignmentId;
    
    /**
     * 职责类型（用于校验，可选）
     */
    private String dutyType;
    
    /**
     * 身份类型（旧版字段，保留以兼容前端）
     * 学员端/志愿者端
     */
    private String identity;
}