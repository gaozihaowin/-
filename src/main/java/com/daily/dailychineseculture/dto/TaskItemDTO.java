package com.daily.dailychineseculture.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务项 DTO
 * 用于今日课程页面中的具体任务
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskItemDTO {
    /**
     * 任务 ID
     */
    private Integer taskId;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务副标题/描述
     */
    private String subtitle;

    /**
     * 是否必做：1-必做，0-选做
     */
    private Integer isRequired;

    /**
     * 是否完成：1-已完成，0-未完成
     */
    private Integer isDone;
}
