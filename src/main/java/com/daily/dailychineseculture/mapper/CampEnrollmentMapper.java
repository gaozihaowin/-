package com.daily.dailychineseculture.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CampEnrollmentMapper {
    Integer countByUserIdAndCampId(@Param("userId") Long userId, @Param("campId") Integer campId);

    int insertEnrollment(@Param("userId") Long userId, @Param("campId") Integer campId);
}
