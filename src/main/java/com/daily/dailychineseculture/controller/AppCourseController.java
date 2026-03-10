package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.CampScheduleDTO;
import com.daily.dailychineseculture.dto.TaskCompleteReqDTO;
import com.daily.dailychineseculture.dto.TaskCompleteRespDTO;
import com.daily.dailychineseculture.dto.TodayCourseDTO;
import com.daily.dailychineseculture.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * 获取指定营期的今日课程
     * GET /courses/{campId}/today
     * 
     * @param campId 营期 ID
     * @return 今日课程信息
     */
    @GetMapping("/{campId}/today")
   public Result<TodayCourseDTO> getTodayCourse(@PathVariable Integer campId) {
        TodayCourseDTO todayCourse = courseService.getTodayCourse(campId);
        return Result.success(todayCourse);
    }
    
    /**
     * 完成任务打卡并返回最新进度
     * POST /courses/plan/{planId}/task/complete
     * 
     * @param planId 计划 ID
     * @param req 请求参数
     * @return 任务完成响应
     */
    @PostMapping("/plan/{planId}/task/complete")
   public Result<TaskCompleteRespDTO> completeTask(@PathVariable Integer planId, @RequestBody TaskCompleteReqDTO req) {
        TaskCompleteRespDTO resp = courseService.completeTask(planId, req);
        return Result.success(resp);
    }
}