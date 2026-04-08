package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.DailyHeatmapDTO;
import com.daily.dailychineseculture.service.DailyRecordService;
import com.daily.dailychineseculture.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/daily-record")
@RequiredArgsConstructor
public class DailyRecordController {

    private final DailyRecordService dailyRecordService;
    private final JwtUtils jwtUtils;

    @GetMapping("/heatmap")
    public ResponseResult<List<DailyHeatmapDTO>> getHeatmap(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        Long userId = jwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            return ResponseResult.error(401, "无效的token");
        }
        List<DailyHeatmapDTO> result = dailyRecordService.getHeatmap(userId, year, month);
        return ResponseResult.success(result);
    }
}
