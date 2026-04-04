package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.dto.HomeworkDetailDTO;
import com.daily.dailychineseculture.dto.HomeworkListDTO;
import com.daily.dailychineseculture.dto.HomeworkHierarchyDTO;
import com.daily.dailychineseculture.dto.HomeworkStatisticsHierarchyDTO;
import com.daily.dailychineseculture.service.HomeworkService;
import com.daily.dailychineseculture.util.JwtUtils;
import com.daily.dailychineseculture.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 作业控制器
 */
@RestController
public class HomeworkController {

    @Autowired
    private HomeworkService homeworkService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 获取作业列表
     */
    @GetMapping("/homework/list")
    public Result<HomeworkListDTO> getHomeworkList(@RequestHeader("Authorization") String token,
                                                   @RequestParam("type") String type,
                                                   @RequestParam("id") Integer id,
                                                   @RequestParam(value = "status", required = false) String status,
                                                   @RequestParam(value = "date", required = false) String date) {
        try {
            // 验证token并获取用户ID
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 调用服务获取作业列表
            HomeworkListDTO result = homeworkService.getHomeworkList(userId, type, id, status, date);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取作业列表失败: " + e.getMessage());
        }
    }

    /**
     * 标记小组优秀作业
     */
    @PostMapping("/camp/homework/mark-small-group")
    public Result<Boolean> markSmallGroupExcellent(@RequestHeader("Authorization") String token,
                                                   @RequestParam(value = "homeworkId", required = false) String homeworkIdStr,
                                                   @RequestParam("isSmallGroupExcellent") Integer isSmallGroupExcellent) {
        try {
            // 验证token
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 验证homeworkId
            if (homeworkIdStr == null || "".equals(homeworkIdStr) || "null".equals(homeworkIdStr)) {
                return Result.error("作业ID不能为空");
            }

            Integer homeworkId;
            try {
                homeworkId = Integer.valueOf(homeworkIdStr);
            } catch (NumberFormatException e) {
                return Result.error("作业ID格式错误");
            }

            // 调用服务标记小组优秀作业
            boolean result = homeworkService.markSmallGroupExcellent(homeworkId, isSmallGroupExcellent);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("标记小组优秀作业失败: " + e.getMessage());
        }
    }

    /**
     * 标记大组优秀作业
     */
    @PostMapping("/camp/homework/mark-big-group")
    public Result<Boolean> markBigGroupExcellent(@RequestHeader("Authorization") String token,
                                                 @RequestParam(value = "homeworkId", required = false) String homeworkIdStr,
                                                 @RequestParam("isBigGroupExcellent") Integer isBigGroupExcellent) {
        try {
            // 验证token
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 验证homeworkId
            if (homeworkIdStr == null || "".equals(homeworkIdStr) || "null".equals(homeworkIdStr)) {
                return Result.error("作业ID不能为空");
            }

            Integer homeworkId;
            try {
                homeworkId = Integer.valueOf(homeworkIdStr);
            } catch (NumberFormatException e) {
                return Result.error("作业ID格式错误");
            }

            // 调用服务标记大组优秀作业
            boolean result = homeworkService.markBigGroupExcellent(homeworkId, isBigGroupExcellent);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("标记大组优秀作业失败: " + e.getMessage());
        }
    }

    /**
     * 获取作业详情
     */
    @GetMapping("/homework/detail")
    public Result<HomeworkDetailDTO> getHomeworkDetail(@RequestHeader("Authorization") String token,
                                                       @RequestParam(value = "homeworkId", required = false) String homeworkIdStr) {
        try {
            // 验证token
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 验证homeworkId
            if (homeworkIdStr == null || "".equals(homeworkIdStr) || "null".equals(homeworkIdStr)) {
                return Result.error("作业ID不能为空");
            }

            Integer homeworkId;
            try {
                homeworkId = Integer.valueOf(homeworkIdStr);
            } catch (NumberFormatException e) {
                return Result.error("作业ID格式错误");
            }

            // 调用服务获取作业详情
            HomeworkDetailDTO result = homeworkService.getHomeworkDetail(homeworkId);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取作业详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取作业统计数据
     */
    @GetMapping("/homework/statistics")
    public Result<Map<String, Object>> getHomeworkStatistics(@RequestHeader("Authorization") String token,
                                                             @RequestParam("type") String type,
                                                             @RequestParam("id") Integer id,
                                                             @RequestParam("date") String date) {
        try {
            // 验证token并获取用户ID
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 调用服务获取作业统计数据
            Map<String, Object> result = homeworkService.getHomeworkStatistics(userId, type, id, date);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取作业统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取作业状态名单（已交/未交/迟交）
     */
    @GetMapping("/homework/status-list")
    public Result<Map<String, Object>> getHomeworkStatusList(@RequestHeader("Authorization") String token,
                                                             @RequestParam("type") String type,
                                                             @RequestParam("id") Integer id,
                                                             @RequestParam("date") String date) {
        try {
            // 验证token并获取用户ID
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 调用服务获取作业状态名单
            Map<String, Object> result = homeworkService.getHomeworkStatusList(userId, type, id, date);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取作业状态名单失败: " + e.getMessage());
        }
    }

    /**
     * 获取优秀作业列表
     */
    @GetMapping("/homework/excellent/list")
    public Result<HomeworkListDTO> getExcellentHomeworkList(@RequestHeader("Authorization") String token,
                                                            @RequestParam("type") String type,
                                                            @RequestParam("id") Integer id,
                                                            @RequestParam(value = "date", required = false) String date) {
        try {
            // 验证token并获取用户ID
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 调用服务获取优秀作业列表
            HomeworkListDTO result = homeworkService.getHomeworkList(userId, type, id, "excellent", date);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取优秀作业列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取作业层级列表（大组-小组-成员）
     */
    @GetMapping("/homework/hierarchy/list")
    public Result<HomeworkHierarchyDTO> getHomeworkHierarchyList(@RequestHeader("Authorization") String token,
                                                                 @RequestParam(value = "date", required = false) String date,
                                                                 @RequestParam(value = "status", required = false) String status,
                                                                 @RequestParam(value = "dutyType", required = false) String dutyType,
                                                                 @RequestParam(value = "targetId", required = false) Integer targetId) {
        try {
            // 验证token并获取用户ID
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 调用服务获取作业层级列表
            HomeworkHierarchyDTO result = homeworkService.getHomeworkHierarchyList(userId, date, status, dutyType, targetId);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取作业层级列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取作业统计层级数据
     */
    @GetMapping("/homework/statistics/hierarchy")
    public Result<HomeworkStatisticsHierarchyDTO> getHomeworkStatisticsHierarchy(@RequestHeader("Authorization") String token,
                                                                                 @RequestParam("type") String type,
                                                                                 @RequestParam("id") Integer id,
                                                                                 @RequestParam("date") String date) {
        try {
            // 验证token并获取用户ID
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("无效的token");
            }

            // 调用服务获取作业统计层级数据
            HomeworkStatisticsHierarchyDTO result = homeworkService.getHomeworkStatisticsHierarchy(userId, type, id, date);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取作业统计层级数据失败: " + e.getMessage());
        }
    }
}