package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.entity.Course;
import com.github.pagehelper.PageInfo;

/**
 * 课程列表服务接口
 * 提供课程分页查询相关业务逻辑
 */
public interface CourseListService {
    
    /**
     * 分页获取课程列表
     * 
     * @param pageNum 页码，默认 1
     * @param pageSize 每页大小，默认 10
     * @return 分页后的课程列表信息
     */
    PageInfo<Course> getCourseList(Integer pageNum, Integer pageSize);
}
