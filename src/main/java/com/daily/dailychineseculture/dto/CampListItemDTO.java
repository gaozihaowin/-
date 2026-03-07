package com.daily.dailychineseculture.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 营期列表项 DTO
 * 用于 PC端后台管理系统 - 营期管理大盘
 */
@Data
public class CampListItemDTO {
    
    /**
     * 营期 ID
     */
    @JsonProperty("campId")
    private Integer campId;
    
    /**
     * 类型名称（来自 t_camp_type.level_name）
     */
    @JsonProperty("typeName")
    private String typeName;
    
    /**
     * 期数
     */
    @JsonProperty("term")
    private Integer term;
    
    /**
     * 营期名称
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * 开营时间（格式：yyyy-MM-dd）
     */
    @JsonProperty("startTime")
    private String startTime;
    
    /**
     * 结营时间（格式：yyyy-MM-dd）
     */
    @JsonProperty("endTime")
    private String endTime;
    
    /**
     * 状态：0-待开课，1-进行中，2-已结束
     */
    @JsonProperty("status")
    private Integer status;
    
    /**
     * 标签（如：热招）
     */
    @JsonProperty("tag")
    private String tag;
    
    /**
     * 报名人数
     */
    @JsonProperty("enrollCount")
    private Integer enrollCount;
}
