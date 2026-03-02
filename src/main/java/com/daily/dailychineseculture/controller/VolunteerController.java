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
     * 退出担当（更新志愿者服务结束时间）
     */
    @PostMapping("/user/volunteer-quit")
    public ResponseResult<String> quitVolunteerDuty(@RequestHeader("Authorization") String token,
                                                    @RequestBody Map<String, Object> request) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            Integer assignmentId = (Integer) request.get("assignmentId");

            if (assignmentId == null) {
                return ResponseResult.error("assignmentId不能为空");
            }

            boolean success = userService.quitVolunteerDuty(userId, assignmentId);
            if (success) {
                return ResponseResult.success("退出担当成功");
            } else {
                return ResponseResult.error("退出担当失败，职责任命不存在或不属于该用户");
            }
        } catch (Exception e) {
            return ResponseResult.error("退出担当失败：" + e.getMessage());
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