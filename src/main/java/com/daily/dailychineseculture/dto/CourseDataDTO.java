package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.List;

/**
 * 课程数据看板 DTO
 */
@Data
public class CourseDataDTO {
    /**
     * 总天数（排课总数）
     */
    private Integer totalDays;
    
    /**
     * 已完成天数（完成率 100% 的天数）
     */
    private Integer completedDays;
    
    /**
     * 总体完成率百分比
     */
    private Integer overallRate;
    
    /**
     * 学习趋势（最近 7 节课）
     */
    private List<TrendItemDTO> trends;
    
    /**
     * 成就徽章列表
     */
    private List<AchievementDTO> achievements;
}
