package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 学习趋势项 DTO
 */
@Data
public class TrendItemDTO {
    /**
     * 日期标识，如 "Day10"
     */
    private String dayStr;
    
    /**
     * 完成率百分比 (0-100)
     */
    private Integer rate;
}
