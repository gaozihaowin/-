package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.MyCourseVO;

import java.util.List;

/**
 * 课程服务接口
 * 提供课程相关业务逻辑
 * 
 * @author Java后端架构师
 * @since 2026-02-25
 */
public interface CourseService {
    
    /**
     * 根据用户ID和标签类型获取我的课程列表
     * 
     * @param userId 用户ID
     * @param tabType 标签类型：1-正在学习, 2-历史课程, 3-已结业
     * @return 我的课程列表
     */
    List<MyCourseVO> getMyCourses(Long userId, Integer tabType);
}