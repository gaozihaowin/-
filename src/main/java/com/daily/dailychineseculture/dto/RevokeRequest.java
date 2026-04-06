package com.daily.dailychineseculture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RevokeRequest {
    @NotNull(message = "权限记录ID不能为空")
    private Integer assignmentId;

    @NotBlank(message = "撤销原因不能为空")
    private String reason;
}
