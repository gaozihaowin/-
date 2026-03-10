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
     * 任务 ID（前端用）：read, video, homework, extra1, extra2, extra3
     */
    private String taskId;

    /**
     * 任务类型：FIXED(固定任务) 或 EXTRA(备选任务)
     */
    private String taskType;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务副标题
     */
    private String subtitle;

    /**
     * 完成状态：0 未完成，1 已完成
     */
    private Integer isDone;
}
