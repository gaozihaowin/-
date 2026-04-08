package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.RadarStatsDTO;
import com.daily.dailychineseculture.dto.StudyArchiveDTO;
import com.daily.dailychineseculture.mapper.UserStatsMapper;
import com.daily.dailychineseculture.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserStatsServiceImpl implements UserStatsService {

    private final UserStatsMapper userStatsMapper;

    @Override
    public StudyArchiveDTO getStudyArchive(Long userId) {
        StudyArchiveDTO dto = new StudyArchiveDTO();
        dto.setTotalCamps(userStatsMapper.countEnrollments(userId));
        dto.setTotalCertificates(userStatsMapper.countCertificates(userId));
        Double avg = userStatsMapper.avgCompletionRate(userId);
        dto.setAvgCompletionRate(avg != null ? avg.intValue() : 0);
        dto.setRadarStats(userStatsMapper.getRadarStats(userId));
        return dto;
    }
}
