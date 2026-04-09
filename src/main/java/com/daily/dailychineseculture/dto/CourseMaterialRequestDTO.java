package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 课件请求 DTO
 */
@Data
public class CourseMaterialRequestDTO {

    /**
     * 课件 ID（修改时传入）
     */
    private Long materialId;

    /**
     * 关联分类 ID
     */
    private Long categoryId;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 类型: READ(阅读/文档), VIDEO(视频), AUDIO(音频)
     */
    private String type;

    /**
     * 云存储链接
     */
    private String url;

    /**
     * 文件大小(字节)
     */
    private Long size;

    /**
     * 时长(秒)
     */
    private Integer duration;
}
