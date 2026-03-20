package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.Date;

/**
 * 排课计划 DTO
 */
@Data
public class CampPlanDTO {
    /**
     * 计划 ID
     */
    private Integer planId;
    
    /**
     * 营期 ID
     */
    private Integer campId;
    
    /**
     * 第几天
     */
    private Integer dayIndex;
    
    /**
     * 具体日期
     */
    private Date planDate;
    
    /**
     * 导读标题
     */
    private String title;
}
