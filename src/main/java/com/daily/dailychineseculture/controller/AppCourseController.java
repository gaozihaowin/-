package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.CampScheduleDTO;
import com.daily.dailychineseculture.dto.CourseDataDTO;
import com.daily.dailychineseculture.dto.TaskCompleteReqDTO;
import com.daily.dailychineseculture.dto.TaskCompleteRespDTO;
import com.daily.dailychineseculture.dto.TodayCourseDTO;
import com.daily.dailychineseculture.dto.CampInfoDTO;
import com.daily.dailychineseculture.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 微信小程序端 - 课程安排目录控制器
 */
@RestController
@RequestMapping("/courses")
public class AppCourseController {

    @Autowired
   private CourseService courseService;

    /**
     * 获取指定营期的课程安排目录
     * GET /courses/{campId}/schedule
     * 
     * @param campId 营期 ID
     * @return 课程安排目录列表
     */
    @GetMapping("/{campId}/schedule")
   public Result<List<CampScheduleDTO>> getCourseSchedule(@PathVariable Integer campId) {
        List<CampScheduleDTO> scheduleList = courseService.getCourseSchedule(campId);
        return Result.success(scheduleList);
    }
    
    /**
     * 获取指定营期的今日课程（支持时光机模式）
     * GET /courses/{campId}/today
     * 
     * @param campId 营期 ID
     * @param planId 排课计划 ID（可选，传入时查询指定历史天）
     * @param request HTTP 请求（用于获取登录用户 ID）
     * @return 今日/历史课程信息
     */
    @GetMapping("/{campId}/today")
    public Result<TodayCourseDTO> getTodayCourse(
            @PathVariable Integer campId,
            @RequestParam(required = false) Integer planId,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录或 Token 失效，无法访问课程");
        }
        TodayCourseDTO todayCourse = courseService.getTodayCourse(campId, currentUserId, planId);
        return Result.success(todayCourse);
    }
    
    /**
     * 完成任务打卡并返回最新进度
     * POST /courses/plan/{planId}/task/complete
     * 
     * @param planId 计划 ID
     * @param req 请求参数
     * @param request HTTP 请求（用于获取登录用户 ID）
     * @return 任务完成响应
     */
    @PostMapping("/plan/{planId}/task/complete")
   public Result<TaskCompleteRespDTO> completeTask(@PathVariable Integer planId, @RequestBody TaskCompleteReqDTO req, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录或 Token 失效，无法打卡");
        }
        TaskCompleteRespDTO resp = courseService.completeTask(planId, req, currentUserId);
        return Result.success(resp);
    }
    
    /**
     * 获取课程数据看板
     * GET /courses/{campId}/data
     * 
     * @param campId 营期 ID
     * @param request HTTP 请求（用于获取登录用户 ID）
     * @return 课程数据看板
     */
    @GetMapping("/{campId}/data")
   public Result<CourseDataDTO> getCourseData(@PathVariable Integer campId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录或 Token 失效，无法访问数据看板");
        }
        CourseDataDTO data = courseService.getCourseData(campId, currentUserId);
        return Result.success(data);
    }
    
    /**
     * 获取营期详情信息（课程详情页顶部信息栏）
     * GET /courses/{campId}/info
     * 
     * @param campId 营期 ID
     * @return 营期详情信息
     */
    @GetMapping("/{campId}/info")
   public Result<CampInfoDTO> getCampInfo(@PathVariable Integer campId) {
        CampInfoDTO info = courseService.getCampInfo(campId);
        return Result.success(info);
    }
}