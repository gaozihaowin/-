package com.daily.dailychineseculture.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 营期列表分页响应 DTO
 * 用于 PC端后台管理系统 - 营期管理大盘
 */
@Data
public class CampListPageDTO {
    
    /**
     * 总记录数
     */
    @JsonProperty("total")
    private Long total;
    
    /**
     * 当前页码
     */
    @JsonProperty("page")
    private Integer page;
    
    /**
     * 每页大小
     */
    @JsonProperty("size")
    private Integer size;
    
    /**
     * 营期列表
     */
    @JsonProperty("list")
    private List<CampListItemDTO> list;
}
