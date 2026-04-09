package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.MaterialCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课件分类 Mapper 接口
 */
@Mapper
public interface MaterialCategoryMapper {

    /**
     * 查询所有分类（用于构建树）
     * @return 分类列表
     */
    List<MaterialCategory> selectAll();

    /**
     * 根据父ID查询子分类数量
     * @param parentId 父分类ID
     * @return 子分类数量
     */
    Integer countByParentId(@Param("parentId") Long parentId);

    /**
     * 根据ID查询分类
     * @param categoryId 分类ID
     * @return 分类实体
     */
    MaterialCategory selectById(@Param("categoryId") Long categoryId);

    /**
     * 新增分类
     * @param category 分类实体
     * @return 影响行数
     */
    Integer insert(MaterialCategory category);

    /**
     * 修改分类
     * @param category 分类实体
     * @return 影响行数
     */
    Integer update(MaterialCategory category);

    /**
     * 删除分类
     * @param categoryId 分类ID
     * @return 影响行数
     */
    Integer deleteById(@Param("categoryId") Long categoryId);
}
