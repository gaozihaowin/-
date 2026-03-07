package com.daily.dailychineseculture.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 最近活跃课程 DTO
 * 用于 PC端后台管理系统仪表盘展示
 */
@Data
public class RecentCampDTO {
    
    /**
     * 营期 ID
     */
    @JsonProperty("campId")
    private Integer campId;
    
    /**
     * 营期名称
     */
    @JsonProperty("campName")
    private String campName;
    
    /**
     * 讲师（固定值）
     */
    @JsonProperty("instructor")
    private String instructor = "致良知教研团队";
    
    /**
     * 访问量（使用报名人数平替）
     */
    @JsonProperty("visitCount")
    private Integer visitCount;
    
    /**
     * 状态码：0-待开课，1-进行中，2-已结束
     */
    @JsonProperty("statusCode")
    private Integer statusCode;
    
    /**
     * 状态文本
     */
    @JsonProperty("statusText")
    private String statusText;
    
    /**
     * 开营时间
     */
    @JsonProperty("startTime")
    private String startTime;
}
