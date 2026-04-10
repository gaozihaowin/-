package com.daily.dailychineseculture.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 课件分类实体类
 * 对应数据库表：t_material_category
 */
@Data
@Alias("MaterialCategory")
public class MaterialCategory {
    private Long categoryId;
    private Long parentId;
    private String name;
    private Integer sort;
}
