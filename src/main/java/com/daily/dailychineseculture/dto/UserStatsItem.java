package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 用户个人信息统计项
 * 用于展示地区、职业、年数、学时等统计指标
 */
@Data
public class UserStatsItem {
    
    /**
     * 统计项标签（如：地区、职业、年数、学时）
     */
    private String label;
    
    /**
     * 统计项的值
     */
    private String value;
}
