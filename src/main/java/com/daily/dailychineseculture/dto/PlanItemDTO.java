package com.daily.dailychineseculture.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程计划项 DTO（微信小程序端 - 课程安排目录）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanItemDTO {
    
    /**
     * 计划 ID（对应 t_camp_plan.plan_id）
     */
   private Integer planId;
    
    /**
     * 第几天（对应 t_camp_plan.day_index）
     */
   private Integer dayIndex;
    
    /**
     * 课程标题（对应 t_camp_plan.title）
     */
   private String title;
    
    /**
     * 阅读篇目（对应 t_camp_plan.reading_title）
     */
   private String readingTitle;
    
    /**
     * 讲师姓名（对应 t_camp_plan.teacher_name）
     */
   private String teacherName;
    
    /**
     * 视频时长（分钟，对应 t_camp_plan.video_duration）
     */
   private Integer videoDuration;
}