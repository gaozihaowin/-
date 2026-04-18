package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.dto.CampEnrollmentDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CampEnrollmentMapper {

    @Select("SELECT COUNT(1) FROM t_camp_enrollment WHERE user_id = #{userId} AND camp_id = #{campId}")
    Integer countByUserIdAndCampId(@Param("userId") Long userId, @Param("campId") Integer campId);

    int insertEnrollment(@Param("userId") Long userId, @Param("campId") Integer campId);

    int insertEnrollmentWithClass(@Param("userId") Long userId, @Param("campId") Integer campId, @Param("classId") Integer classId);

    @Update("UPDATE t_camp_enrollment SET progress = #{progress} " +
            "WHERE user_id = #{userId} AND camp_id = #{campId}")
    int updateProgress(@Param("userId") Long userId,
                       @Param("campId") Integer campId,
                       @Param("progress") Integer progress);

    CampEnrollmentDTO selectByUserIdAndCampId(@Param("userId") Long userId,
                                              @Param("campId") Integer campId);

    int updateCompletionStatus(@Param("userId") Long userId,
                               @Param("campId") Integer campId,
                               @Param("status") int status);

    List<CampEnrollmentDTO> selectEnrollmentsByCampId(@Param("campId") Integer campId);
}
