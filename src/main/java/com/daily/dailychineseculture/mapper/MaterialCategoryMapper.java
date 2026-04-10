package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.dto.MaterialCategorySortDTO;
import com.daily.dailychineseculture.entity.MaterialCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课件分类 Mapper 接口
 */
@Mapper
public interface MaterialCategoryMapper {

    List<MaterialCategory> selectAll();

    Integer countByParentId(@Param("parentId") Long parentId);

    MaterialCategory selectById(@Param("categoryId") Long categoryId);

    Integer insert(MaterialCategory category);

    Integer update(MaterialCategory category);

    Integer deleteById(@Param("categoryId") Long categoryId);

    Integer selectMaxSortByParentId(@Param("parentId") Long parentId);

    Integer batchUpdateSort(@Param("list") List<MaterialCategorySortDTO> list);

    Integer deleteByIds(@Param("ids") List<Long> ids);
}
