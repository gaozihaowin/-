package com.daily.dailychineseculture.dto;

import lombok.Data;

import java.util.List;

/**
 * 分类树 VO
 */
@Data
public class MaterialCategoryTreeVO {

    /**
     * 分类 ID
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

    /**
     * 子分类列表
     */
    private List<MaterialCategoryTreeVO> children;
}
