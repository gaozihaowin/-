package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.CourseMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMaterialMapper {

    List<CourseMaterial> selectPage(@Param("categoryIds") List<Long> categoryIds,
                                   @Param("type") String type,
                                   @Param("keyword") String keyword);

    Integer countPage(@Param("categoryIds") List<Long> categoryIds,
                      @Param("type") String type,
                      @Param("keyword") String keyword);

    CourseMaterial selectById(@Param("materialId") Long materialId);

    Integer insert(CourseMaterial material);

    Integer update(CourseMaterial material);

    Integer deleteById(@Param("materialId") Long materialId);

    Integer countByCategoryId(@Param("categoryId") Long categoryId);

    Integer countByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

    Integer updatePlanTaskUrl(@Param("materialId") Long materialId, @Param("newUrl") String newUrl);

    Integer countByMaterialId(@Param("materialId") Long materialId);
}
