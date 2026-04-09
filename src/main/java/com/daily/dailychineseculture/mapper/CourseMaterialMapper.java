package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.CourseMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课件资源 Mapper 接口
 */
@Mapper
public interface CourseMaterialMapper {

    /**
     * 分页查询课件
     * @param categoryId 分类ID（可选）
     * @param type 类型（可选）: READ, VIDEO, AUDIO
     * @param keyword 关键词（可选）
     * @return 课件列表
     */
    List<CourseMaterial> selectPage(@Param("categoryId") Long categoryId,
                                   @Param("type") String type,
                                   @Param("keyword") String keyword);

    /**
     * 统计课件总数（用于分页）
     * @param categoryId 分类ID（可选）
     * @param type 类型（可选）: READ, VIDEO, AUDIO
     * @param keyword 关键词（可选）
     * @return 总数
     */
    Integer countPage(@Param("categoryId") Long categoryId,
                     @Param("type") String type,
                     @Param("keyword") String keyword);

    /**
     * 根据ID查询课件
     * @param materialId 课件ID
     * @return 课件实体
     */
    CourseMaterial selectById(@Param("materialId") Long materialId);

    /**
     * 新增课件
     * @param material 课件实体
     * @return 影响行数
     */
    Integer insert(CourseMaterial material);

    /**
     * 修改课件
     * @param material 课件实体
     * @return 影响行数
     */
    Integer update(CourseMaterial material);

    /**
     * 删除课件
     * @param materialId 课件ID
     * @return 影响行数
     */
    Integer deleteById(@Param("materialId") Long materialId);

    /**
     * 根据分类ID统计课件数量
     * @param categoryId 分类ID
     * @return 课件数量
     */
    Integer countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 更新排课任务表的 task_url（同步更新）
     * @param materialId 素材ID
     * @param newUrl 新的URL
     * @return 影响行数
     */
    Integer updatePlanTaskUrl(@Param("materialId") Long materialId, @Param("newUrl") String newUrl);

    /**
     * 统计排课任务引用该素材的数量（防爆拦截）
     * @param materialId 素材ID
     * @return 被引用数量
     */
    Integer countPlanTaskByMaterialId(@Param("materialId") Long materialId);
}
