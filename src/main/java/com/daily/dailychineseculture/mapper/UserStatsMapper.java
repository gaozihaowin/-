package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.dto.RadarStatsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface UserStatsMapper {
    int countEnrollments(@Param("userId") Long userId);

    int countCertificates(@Param("userId") Long userId);

    Double avgCompletionRate(@Param("userId") Long userId);

    RadarStatsDTO getRadarStats(@Param("userId") Long userId);
}
