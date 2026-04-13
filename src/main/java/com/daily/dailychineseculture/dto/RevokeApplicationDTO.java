package com.daily.dailychineseculture.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 撤销申请请求DTO
 */
@Data
public class RevokeApplicationDTO {

    /**
     * 申请ID（必填）
     */
    @NotNull(message = "申请ID不能为空")
    private Integer applyId;
}
