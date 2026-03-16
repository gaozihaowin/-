package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 成就徽章 DTO
 */
@Data
public class AchievementDTO {
    /**
     * 徽章图标 URL
     */
    private String icon;
    
    /**
     * 成就标题
     */
    private String title;
    
    /**
     * 成就描述
     */
    private String desc;
}
