package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.VolunteerHistoryDTO;
import com.daily.dailychineseculture.dto.VolunteerStatsDTO;
import com.daily.dailychineseculture.service.UserService;
import com.daily.dailychineseculture.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 志愿者控制器
 */
@RestController
@RequestMapping("")
public class VolunteerController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 获取用户志愿者历史记录
     */
    @GetMapping("/user/volunteer-history")
    public ResponseResult<VolunteerHistoryDTO> getVolunteerHistory(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            VolunteerHistoryDTO result = userService.getVolunteerHistory(userId);
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.error("获取志愿者过往担当失败");
        }
    }

    /**
     * 获取志愿者统计信息
     */
    @GetMapping("/user/volunteer-stats")
    public ResponseResult<VolunteerStatsDTO> getVolunteerStats(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));

            VolunteerStatsDTO result = userService.getVolunteerStats(userId);
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.error("获取志愿者统计失败：" + e.getMessage());
        }
    }
}