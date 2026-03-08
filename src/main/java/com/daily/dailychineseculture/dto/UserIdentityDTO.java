package com.daily.dailychineseculture.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户身份信息 DTO
 * 用于返回用户可切换的职责身份列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentityDTO {
    
    /**
     * 任命记录 ID（来自 t_duty_assignment.assignment_id）
     */
    @JsonProperty("assignmentId")
    private Integer assignmentId;
    
    /**
     * 职责类型（如：COURSE_ADMIN, 学班）
     */
    @JsonProperty("dutyType")
    private String dutyType;
    
    /**
     * 职责名称（中文显示）
     */
    @JsonProperty("dutyName")
    private String dutyName;
    
    /**
     * 营期 ID（若为全局职务则为 null）
     */
    @JsonProperty("campId")
    private Integer campId;
    
    /**
     * 营期名称（若 campId 为空则返回 "全局教务"）
     */
    @JsonProperty("campName")
    private String campName;
}
