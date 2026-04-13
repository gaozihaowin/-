package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 课件分页请求 DTO
 */
@Data
public class CourseMaterialPageDTO {

    /**
     * 分类 ID（可选）
     */
    private Long categoryId;

    /**
     * 类型（可选）: READ(阅读/文档), VIDEO(视频), AUDIO(音频)
     */
    private String type;

    /**
     * 关键词（可选，模糊匹配 name）
     */
    private String keyword;

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;
}
