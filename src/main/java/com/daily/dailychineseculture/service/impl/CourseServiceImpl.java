package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.CampPlanDTO;
import com.daily.dailychineseculture.dto.CampScheduleDTO;
import com.daily.dailychineseculture.dto.MyCourseVO;
import com.daily.dailychineseculture.dto.PlanItemDTO;
import com.daily.dailychineseculture.dto.TaskItemDTO;
import com.daily.dailychineseculture.dto.TodayCourseDTO;
import com.daily.dailychineseculture.entity.CampPlan;
import com.daily.dailychineseculture.entity.UserDailyRecord;
import com.daily.dailychineseculture.mapper.CampPlanMapper;
import com.daily.dailychineseculture.mapper.MyCourseMapper;
import com.daily.dailychineseculture.mapper.UserDailyRecordMapper;
import com.daily.dailychineseculture.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程服务实现类
 * 实现课程相关业务逻辑
 * 
 * @author Java后端架构师
 * @since 2026-02-25
 */
@Service
public class CourseServiceImpl implements CourseService {
    
    @Autowired
   private MyCourseMapper myCourseMapper;
    
    @Autowired
   private CampPlanMapper campPlanMapper;
    
    @Autowired
   private UserDailyRecordMapper userDailyRecordMapper;
    
    @Override
   public List<MyCourseVO> getMyCourses(Long userId, Integer tabType) {
        // 参数校验
        if (userId == null || tabType == null) {
            throw new IllegalArgumentException("用户ID和标签类型不能为空");
        }
        
        if (tabType < 1 || tabType > 3) {
            throw new IllegalArgumentException("标签类型必须为1、2或3");
        }
        
        // 查询我的课程列表
        return myCourseMapper.selectMyCourses(userId, tabType);
    }
    
    @Override
   public List<CampScheduleDTO> getCourseSchedule(Integer campId) {
        // 1. 查询营期的所有课程计划（已按 day_index 升序）
        List<CampPlan> allPlans = campPlanMapper.selectCourseScheduleByCampId(campId);
        
        if (allPlans == null || allPlans.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 按 moduleIndex 分组
        Map<Integer, List<CampPlan>> groupedByModule = allPlans.stream()
                .collect(Collectors.groupingBy(CampPlan::getModuleIndex));
        
        // 3. 组装 DTO 列表
        List<CampScheduleDTO> scheduleList = new ArrayList<>();
        for (Map.Entry<Integer, List<CampPlan>> entry : groupedByModule.entrySet()) {
            Integer moduleIndex = entry.getKey();
            List<CampPlan> plans = entry.getValue();
            
            // 获取模块名称并拼接中文周次
            String moduleName = plans.get(0).getModuleName();
            String fullModuleName = buildWeekName(moduleIndex, moduleName);
            
            // 转换为 PlanItemDTO 列表
            List<PlanItemDTO> planItems = plans.stream()
                    .map(plan -> new PlanItemDTO(
                            plan.getPlanId(),
                            plan.getDayIndex(),
                            plan.getTitle(),
                            plan.getReadingTitle(),
                            plan.getTeacherName(),
                            plan.getVideoDuration()
                    ))
                    .collect(Collectors.toList());
            
            scheduleList.add(new CampScheduleDTO(moduleIndex, fullModuleName, planItems));
        }
        
        // 4. 按 moduleIndex 升序排序
        scheduleList.sort((a, b) -> a.getModuleIndex().compareTo(b.getModuleIndex()));
        
        return scheduleList;
    }
    
    /**
     * 将阿拉伯数字转为中文周次名称
     * 例如：1 -> "一", 2 -> "二", 3 -> "三"
     * 最终返回："第一周：基础认知"
     */
  private String buildWeekName(Integer moduleIndex, String moduleName) {
        String[] chineseNumbers = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        
        String chineseNumber;
        if (moduleIndex >= 1 && moduleIndex <= 10) {
            chineseNumber = chineseNumbers[moduleIndex];
        } else {
            // 如果超过 10，直接显示数字
            chineseNumber = moduleIndex.toString();
        }
        
        return "第" + chineseNumber + "周：" + moduleName;
    }
    
    @Override
   public TodayCourseDTO getTodayCourse(Integer campId) {
        // 1. 获取今日日期并格式化
        java.time.LocalDate today = java.time.LocalDate.now();
        String currentDate = today.format(java.time.format.DateTimeFormatter.ofPattern("M 月 d 日"));
        
        // 2. 查询今日排课
        CampPlan todayPlan = campPlanMapper.selectTodayPlan(campId, java.sql.Date.valueOf(today));
        
        // 3. 今日无课兜底
        if (todayPlan == null) {
            TodayCourseDTO noCourseDTO = new TodayCourseDTO();
            noCourseDTO.setHasCourse(false);
            noCourseDTO.setCurrentDate(currentDate);
            noCourseDTO.setPlanId(null);
            noCourseDTO.setCompletionRate(0);
            noCourseDTO.setTasks(new ArrayList<>());
           return noCourseDTO;
        }
        
        // 4. 今日有课组装
        TodayCourseDTO dto = new TodayCourseDTO();
        dto.setHasCourse(true);
        dto.setCurrentDate(currentDate);
        dto.setPlanId(todayPlan.getPlanId());
        
        // 硬编码测试用户 ID
        Long currentUserId = 10001L;
        
        // 查询用户今日学习记录
        UserDailyRecord record = userDailyRecordMapper.selectByUserIdPlanIdAndDate(
            currentUserId, 
            todayPlan.getPlanId(), 
            java.sql.Date.valueOf(today)
        );
        
        // 装配任务列表
        List<TaskItemDTO> tasks = new ArrayList<>();
        int completedCount = 0;
        
        // 固定任务 1: 原文诵读
        TaskItemDTO readTask = new TaskItemDTO();
        readTask.setTaskId("read");
        readTask.setTaskType("FIXED");
        readTask.setTitle("原文诵读");
        readTask.setSubtitle(todayPlan.getReadingTitle() != null ? todayPlan.getReadingTitle() : "");
        readTask.setIsDone((record != null && record.getReadStatus() != null && record.getReadStatus() == 1) ? 1 : 0);
        tasks.add(readTask);
        if (readTask.getIsDone() == 1) completedCount++;
        
        // 固定任务 2: 名师导读
        TaskItemDTO videoTask = new TaskItemDTO();
        videoTask.setTaskId("video");
        videoTask.setTaskType("FIXED");
        videoTask.setTitle("名师导读");
        String videoSubtitle = buildVideoSubtitle(todayPlan.getTeacherName(), todayPlan.getVideoDuration());
        videoTask.setSubtitle(videoSubtitle);
        videoTask.setIsDone((record != null && record.getVideoStatus() != null && record.getVideoStatus() == 1) ? 1 : 0);
        tasks.add(videoTask);
        if (videoTask.getIsDone() == 1) completedCount++;
        
        // 固定任务 3: 心得打卡
        TaskItemDTO homeworkTask = new TaskItemDTO();
        homeworkTask.setTaskId("homework");
        homeworkTask.setTaskType("FIXED");
        homeworkTask.setTitle("心得打卡");
        homeworkTask.setSubtitle("写下今日感悟");
        homeworkTask.setIsDone((record != null && record.getHomeworkStatus() != null && record.getHomeworkStatus() == 1) ? 1 : 0);
        tasks.add(homeworkTask);
        if (homeworkTask.getIsDone() == 1) completedCount++;
        
        // 备选任务 1
        if (todayPlan.getExtraTask1Name() != null && !todayPlan.getExtraTask1Name().isEmpty()) {
            TaskItemDTO extraTask1 = new TaskItemDTO();
            extraTask1.setTaskId("extra1");
            extraTask1.setTaskType("EXTRA");
            extraTask1.setTitle(todayPlan.getExtraTask1Name());
            extraTask1.setSubtitle("");
            extraTask1.setIsDone((record != null && record.getExtraTask1Status() != null && record.getExtraTask1Status() == 1) ? 1 : 0);
            tasks.add(extraTask1);
            if (extraTask1.getIsDone() == 1) completedCount++;
        }
        
        // 备选任务 2
        if (todayPlan.getExtraTask2Name() != null && !todayPlan.getExtraTask2Name().isEmpty()) {
            TaskItemDTO extraTask2 = new TaskItemDTO();
            extraTask2.setTaskId("extra2");
            extraTask2.setTaskType("EXTRA");
            extraTask2.setTitle(todayPlan.getExtraTask2Name());
            extraTask2.setSubtitle("");
            extraTask2.setIsDone((record != null && record.getExtraTask2Status() != null && record.getExtraTask2Status() == 1) ? 1 : 0);
            tasks.add(extraTask2);
            if (extraTask2.getIsDone() == 1) completedCount++;
        }
        
        // 备选任务 3
        if (todayPlan.getExtraTask3Name() != null && !todayPlan.getExtraTask3Name().isEmpty()) {
            TaskItemDTO extraTask3 = new TaskItemDTO();
            extraTask3.setTaskId("extra3");
            extraTask3.setTaskType("EXTRA");
            extraTask3.setTitle(todayPlan.getExtraTask3Name());
            extraTask3.setSubtitle("");
            extraTask3.setIsDone((record != null && record.getExtraTask3Status() != null && record.getExtraTask3Status() == 1) ? 1 : 0);
            tasks.add(extraTask3);
            if (extraTask3.getIsDone() == 1) completedCount++;
        }
        
        // 计算完成率
        int totalTasks = tasks.size();
        int completionRate = totalTasks > 0 ? (completedCount * 100 / totalTasks) : 0;
        dto.setCompletionRate(completionRate);
        
        dto.setTasks(tasks);
       return dto;
    }
    
    /**
     * 构建视频任务副标题
     * 格式："老师姓名 · 时长分钟深度解析"
     */
   private String buildVideoSubtitle(String teacherName, Integer videoDuration) {
        if (teacherName == null || teacherName.isEmpty()) {
           return videoDuration + "分钟深度解析";
        }
       return teacherName + " · " + videoDuration + "分钟深度解析";
    }
}