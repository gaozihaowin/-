package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 审批流转请求DTO
 */
@Data
public class DutyApplicationReviewDTO {

    /**
     * 申请ID
     */
    private Integer applyId;

    /**
     * 审批状态：1-通过, 2-拒绝
     */
    private Integer status;

    /**
     * 审核备注（拒绝时必填，通过时选填）
     */
    private String auditRemark;
}
