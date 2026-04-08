package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.DailyHeatmapDTO;
import com.daily.dailychineseculture.mapper.UserDailyRecordMapper;
import com.daily.dailychineseculture.service.DailyRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyRecordServiceImpl implements DailyRecordService {

    private final UserDailyRecordMapper userDailyRecordMapper;

    @Override
    public List<DailyHeatmapDTO> getHeatmap(Long userId, Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        int currentYear = year != null ? year : now.getYear();
        int currentMonth = month != null ? month : now.getMonthValue();
        return userDailyRecordMapper.selectHeatmap(userId, currentYear, currentMonth);
    }
}
