package com.daily.dailychineseculture.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 课件资源实体类
 * 对应数据库表：t_course_material
 */
@Data
@Alias("CourseMaterial")
public class CourseMaterial {

    /**
     * 资源 ID
     */
    private Long materialId;

    /**
     * 关联分类 ID
     */
    private Long categoryId;

    /**
     * 关联分类名称
     */
    private String categoryName;

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

    /**
     * 上传人
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
