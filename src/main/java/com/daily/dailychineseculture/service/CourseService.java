package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.CampScheduleDTO;
import com.daily.dailychineseculture.dto.CourseDataDTO;
import com.daily.dailychineseculture.dto.MyCourseVO;
import com.daily.dailychineseculture.dto.TaskCompleteRespDTO;
import com.daily.dailychineseculture.dto.TodayCourseDTO;
import com.daily.dailychineseculture.dto.TaskCompleteReqDTO;
import com.daily.dailychineseculture.dto.CampInfoDTO;
import com.daily.dailychineseculture.entity.Course;

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
    
    /**
     * 获取指定营期的课程安排目录
     * 
     * @param campId 营期 ID
     * @return 课程安排目录列表
     */
   List<CampScheduleDTO> getCourseSchedule(Integer campId);
    
    /**
     * 获取指定营期的今日课程（微信小程序端）
     * 
     * @param campId 营期 ID
     * @param userId 当前登录用户 ID
     * @return 今日课程信息
     */
    TodayCourseDTO getTodayCourse(Integer campId, Long userId);
    
    /**
     * 完成任务打卡并返回最新进度（微信小程序端）
     * 
     * @param planId 计划 ID
     * @param req 请求参数
     * @param userId 当前登录用户 ID
     * @return 任务完成响应
     */
    TaskCompleteRespDTO completeTask(Integer planId, TaskCompleteReqDTO req, Long userId);
    
    /**
     * 获取课程数据看板（微信小程序端）
     * 
     * @param campId 营期 ID
     * @param userId 当前登录用户 ID
     * @return 课程数据看板
     */
    CourseDataDTO getCourseData(Integer campId, Long userId);
    
    /**
     * 获取营期详情信息（移动端课程详情页顶部信息栏）
     * 
     * @param campId 营期 ID
     * @return 营期详情信息
     */
    CampInfoDTO getCampInfo(Integer campId);

    Course getCourseDetail(Integer id);
}
