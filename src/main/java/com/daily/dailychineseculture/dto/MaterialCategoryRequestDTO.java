package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 分类请求 DTO
 */
@Data
public class MaterialCategoryRequestDTO {

    /**
     * 分类 ID（修改时传入）
     */
    private Long categoryId;

    /**
     * 父分类 ID
     */
    private Long parentId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;
}
