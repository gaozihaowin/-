package com.daily.dailychineseculture.dto;

import lombok.Data;

@Data
public class StudyArchiveDTO {
    private Integer totalCamps;
    private Integer totalCertificates;
    private Integer avgCompletionRate;
    private RadarStatsDTO radarStats;
}
