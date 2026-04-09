package com.daily.dailychineseculture.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 课件分类实体类
 * 对应数据库表：t_material_category
 */
@Data
@Alias("MaterialCategory")
public class MaterialCategory {

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 父分类 ID，0 为根节点
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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
