package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.MyCourseVO;
import com.daily.dailychineseculture.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程控制器
 * 提供课程相关API接口
 * 
 * @author Java后端架构师
 * @since 2026-02-25
 */
@RestController
@RequestMapping("/courses")
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    /**
     * 获取我的课程列表
     * 
     * @param userId 用户ID
     * @param tabType 标签类型：1-正在学习, 2-历史课程, 3-已结业
     * @return 我的课程列表
     */
    @GetMapping
    public Result<List<MyCourseVO>> getMyCourses(
            @RequestParam Long userId,
            @RequestParam Integer tabType) {
        
        try {
            List<MyCourseVO> myCourses = courseService.getMyCourses(userId, tabType);
            return Result.success(myCourses);
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("获取我的课程失败: " + e.getMessage());
        }
    }
}