package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.entity.Course;
import com.daily.dailychineseculture.service.CourseListService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课程列表控制器
 * 提供C端微信小程序课程分页查询API接口
 */
@RestController
@RequestMapping("/courses")
public class CourseListController {

    @Autowired
    private CourseListService courseListService;

    /**
     * 获取全部正在开课的营期列表（分页）
     * 接口路径：GET /courses/list
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页大小，默认 10
     * @return 统一响应结果，包含分页课程列表
     */
    @GetMapping("/list")
    public Result<PageInfo<Course>> getCourseList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Course> pageInfo = courseListService.getCourseList(pageNum, pageSize);
        return Result.success(pageInfo);
    }
}
