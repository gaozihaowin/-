package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.StudyArchiveDTO;

public interface UserStatsService {
    StudyArchiveDTO getStudyArchive(Long userId);
}
