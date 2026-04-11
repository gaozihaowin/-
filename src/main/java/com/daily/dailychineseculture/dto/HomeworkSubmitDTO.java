package com.daily.dailychineseculture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HomeworkSubmitDTO {

    @NotNull(message = "排课计划ID不能为空")
    private Integer planId;

    @NotNull(message = "任务ID不能为空")
    private Integer taskId;

    @NotBlank(message = "作业内容不能为空")
    private String content;
}
