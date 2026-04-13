package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 新生分班请求DTO
 */
@Data
public class ClassAssignRequestDTO {

    /**
     * 营期ID
     */
    private Integer campId;

    /**
     * 要分的班级数量
     */
    private Integer classCount;

    /**
     * 每班人数上限（可选，不传则自动计算）
     */
    private Integer perClassLimit;
}