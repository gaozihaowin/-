package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.DailyHeatmapDTO;
import java.util.List;

public interface DailyRecordService {
    List<DailyHeatmapDTO> getHeatmap(Long userId, Integer year, Integer month);
}
