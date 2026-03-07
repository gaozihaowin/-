package com.daily.dailychineseculture.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 营期类型选项 DTO
 * 用于前端下拉框展示
 */
@Data
public class CampTypeOptionDTO {
    
    /**
     * 类型 ID
     */
    @JsonProperty("typeId")
    private Integer typeId;
    
    /**
     * 等级名称（如：明理班、笃行班等）
     */
    @JsonProperty("levelName")
    private String levelName;
}
