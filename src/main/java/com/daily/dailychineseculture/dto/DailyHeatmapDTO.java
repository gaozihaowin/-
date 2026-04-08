package com.daily.dailychineseculture.dto;

import lombok.Data;

@Data
public class DailyHeatmapDTO {
    private String date;
    private Integer completionRate;
    private Integer isAllCompleted;
}
