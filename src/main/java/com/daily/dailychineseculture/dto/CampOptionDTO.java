package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 营期下拉选项 DTO
 */
@Data
public class CampOptionDTO {
    /**
     * 营期 ID
     */
    private Integer campId;
    
    /**
     * 营期名称
     */
    private String name;
    
    /**
     * 期数
     */
    private Integer term;
}
