package com.daily.dailychineseculture.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CampEnrollmentMapper {
    Integer countByUserIdAndCampId(@Param("userId") Long userId, @Param("campId") Integer campId);

    int insertEnrollment(@Param("userId") Long userId, @Param("campId") Integer campId);

    @Update("UPDATE t_camp_enrollment SET progress = #{progress} WHERE user_id = #{userId} AND camp_id = #{campId}")
    int updateProgress(@Param("userId") Long userId, @Param("campId") Integer campId, @Param("progress") Integer progress);
}
