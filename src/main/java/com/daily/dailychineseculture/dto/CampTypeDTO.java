package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 营期类型 DTO
 * 用于课程体系分类管理
 */
@Data
public class CampTypeDTO {
    /**
     * 类型 ID
     */
    private Integer typeId;
    
    /**
     * 等级标识（如：ML、DX、CY 等）
     */
    private String level;
    
    /**
     * 等级名称（如：明理班、笃行班、诚意班等）
     */
    private String levelName;
}
