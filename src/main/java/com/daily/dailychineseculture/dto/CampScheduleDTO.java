package com.daily.dailychineseculture.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 课程安排目录 DTO（微信小程序端 - 课程安排目录）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampScheduleDTO {
    
    /**
     * 模块索引（第几周，对应 t_camp_plan.module_index）
     */
  private Integer moduleIndex;
    
    /**
     * 模块名称（后端拼接后的完整字符串，如 "第一周：基础认知"）
     */
  private String moduleName;
    
    /**
     * 该模块下的所有课程计划
     */
  private List<PlanItemDTO> plans;
}