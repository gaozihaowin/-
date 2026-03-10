package com.daily.dailychineseculture.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务完成响应 DTO
 * 用于微信小程序端打卡接口返回
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompleteRespDTO {
    /**
     * 计划 ID
     */
    private Integer planId;

    /**
     * 任务类型：read, video, homework, extra1, extra2
     */
    private String taskType;

    /**
     * 完成率（百分比）：如 60 表示 60%
     */
    private Integer completionRate;
}
