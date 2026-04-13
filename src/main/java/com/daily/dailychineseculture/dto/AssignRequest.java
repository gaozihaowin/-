package com.daily.dailychineseculture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "权限类型不能为空")
    private String dutyType;

    @NotBlank(message = "任命原因不能为空")
    private String reason;
}
