package com.daily.dailychineseculture.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 今日课程 DTO
 * 用于微信小程序端今日课程页面
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodayCourseDTO {
    /**
     * 是否有课：true-有课，false-无课
     */
    private Boolean hasCourse;

    /**
     * 当前日期（格式化后）：如 "3 月 10 日"
     */
    private String currentDate;

    /**
     * 计划 ID
     */
    private Integer planId;

    /**
     * 完成率（百分比）：如 40 表示 40%
     */
    private Integer completionRate;

    /**
     * 任务列表
     */
    private List<TaskItemDTO> tasks;
}
