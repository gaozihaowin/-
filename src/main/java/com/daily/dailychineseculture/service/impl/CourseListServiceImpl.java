package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.entity.Course;
import com.daily.dailychineseculture.mapper.CourseMapper;
import com.daily.dailychineseculture.service.CourseListService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程列表服务实现类
 * 使用PageHelper实现分页查询
 */
@Service
public class CourseListServiceImpl implements CourseListService {

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public PageInfo<Course> getCourseList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Course> list = courseMapper.selectActiveCourses();
        return new PageInfo<>(list);
    }
}
