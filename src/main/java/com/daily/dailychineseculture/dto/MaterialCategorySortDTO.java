package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 分类排序更新 DTO（拖拽排序批量更新）
 */
@Data
public class MaterialCategorySortDTO {

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 父分类 ID
     */
    private Long parentId;

    /**
     * 排序值
     */
    private Integer sort;
}