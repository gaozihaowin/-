package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.CampPlanDTO;
import com.daily.dailychineseculture.dto.CampScheduleDTO;
import com.daily.dailychineseculture.dto.CourseDataDTO;
import com.daily.dailychineseculture.dto.MyCourseVO;
import com.daily.dailychineseculture.dto.PlanItemDTO;
import com.daily.dailychineseculture.dto.TaskCompleteReqDTO;
import com.daily.dailychineseculture.dto.TaskCompleteRespDTO;
import com.daily.dailychineseculture.dto.TaskItemDTO;
import com.daily.dailychineseculture.dto.TodayCourseDTO;
import com.daily.dailychineseculture.dto.TrendItemDTO;
import com.daily.dailychineseculture.dto.AchievementDTO;
import com.daily.dailychineseculture.dto.CampInfoDTO;
import com.daily.dailychineseculture.entity.Course;
import com.daily.dailychineseculture.entity.CampPlan;
import com.daily.dailychineseculture.entity.UserDailyRecord;
import com.daily.dailychineseculture.event.CampProgressUpdateEvent;
import com.daily.dailychineseculture.mapper.CampPlanMapper;
import com.daily.dailychineseculture.mapper.CourseMapper;
import com.daily.dailychineseculture.mapper.MyCourseMapper;
import com.daily.dailychineseculture.mapper.PlanTaskMapper;
import com.daily.dailychineseculture.mapper.UserDailyRecordMapper;
import com.daily.dailychineseculture.mapper.UserTaskRecordMapper;
import com.daily.dailychineseculture.mapper.CampMapper;
import com.daily.dailychineseculture.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程服务实现类
 * 实现课程相关业务逻辑
 * 
 * @author Java 后端架构师
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
    
    @Autowired
   private CampMapper campMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
   private CourseMapper courseMapper;

    @Autowired
   private PlanTaskMapper planTaskMapper;

    @Autowired
   private UserTaskRecordMapper userTaskRecordMapper;
    
    @Override
  public List<MyCourseVO> getMyCourses(Long userId, Integer tabType) {
        // 参数校验
        if (userId == null || tabType == null) {
            throw new IllegalArgumentException("用户 ID 和标签类型不能为空");
        }
        
        if (tabType < 1 || tabType > 3) {
            throw new IllegalArgumentException("标签类型必须为 1、2 或 3");
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
            
            // 获取模块名称并拼接中文周次（容错兜底）
            String rawModuleName = plans.get(0).getModuleName();
            String fullModuleName = buildWeekName(moduleIndex, rawModuleName);
            
            // 转换为 PlanItemDTO 列表
            List<PlanItemDTO> planItems = plans.stream()
                    .map(plan -> new PlanItemDTO(
                            plan.getPlanId(),
                            plan.getDayIndex(),
                            plan.getTitle(),
                            plan.getTeacherName()
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
        if (moduleIndex == null || moduleIndex <= 0) {
            moduleIndex = 1;
        }
        if (moduleName == null) {
            moduleName = "拓展排课";
        }
        if (moduleName.contains("周：") || moduleName.contains("周:")) {
            return moduleName;
        }
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
    public TodayCourseDTO getTodayCourse(Integer campId, Long currentUserId, Integer planId) {
        CampPlan targetPlan = null;
        String displayDateStr = "";

        if (planId != null) {
            targetPlan = campPlanMapper.selectById(planId);
            if (targetPlan == null || !targetPlan.getCampId().equals(campId)) {
                TodayCourseDTO emptyDto = new TodayCourseDTO();
                emptyDto.setHasCourse(false);
                emptyDto.setCurrentDate("");
                emptyDto.setPlanId(null);
                emptyDto.setCompletionRate(0);
                emptyDto.setTasks(new ArrayList<>());
                return emptyDto;
            }
            if (targetPlan.getPlanDate() != null) {
                displayDateStr = targetPlan.getPlanDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                        .format(java.time.format.DateTimeFormatter.ofPattern("M 月 d 日"));
            } else {
                displayDateStr = "Day " + targetPlan.getDayIndex();
            }
        } else {
            java.time.LocalDate today = java.time.LocalDate.now();
            displayDateStr = today.format(java.time.format.DateTimeFormatter.ofPattern("M 月 d 日"));
            targetPlan = campPlanMapper.selectTodayPlan(campId, java.sql.Date.valueOf(today));
        }

        if (targetPlan == null) {
            TodayCourseDTO emptyDto = new TodayCourseDTO();
            emptyDto.setHasCourse(false);
            emptyDto.setCurrentDate(displayDateStr);
            emptyDto.setPlanId(null);
            emptyDto.setCompletionRate(0);
            emptyDto.setTasks(new ArrayList<>());
            return emptyDto;
        }

        TodayCourseDTO dto = new TodayCourseDTO();
        dto.setHasCourse(true);
        dto.setCurrentDate(displayDateStr);
        dto.setPlanId(targetPlan.getPlanId());

        List<TaskItemDTO> tasks = planTaskMapper.selectTaskItemsByPlanIdAndUserId(targetPlan.getPlanId(), currentUserId);
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        dto.setTasks(tasks);

        UserDailyRecord summary = userDailyRecordMapper.selectByUserIdAndPlanId(currentUserId, targetPlan.getPlanId());
        int completionRate = 0;
        if (summary != null && summary.getCompletionRate() != null) {
            completionRate = summary.getCompletionRate();
        } else if (!tasks.isEmpty()) {
            int doneCount = 0;
            for (TaskItemDTO task : tasks) {
                if (task.getIsDone() != null && task.getIsDone() == 1) {
                    doneCount++;
                }
            }
            completionRate = doneCount * 100 / tasks.size();
        }
        dto.setCompletionRate(completionRate);
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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
  public TaskCompleteRespDTO completeTask(Integer planId, TaskCompleteReqDTO req, Long currentUserId) {
        if (req == null || req.getTaskId() == null) {
            throw new IllegalArgumentException("taskId 不能为空");
        }

        CampPlan plan = campPlanMapper.selectById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("排课计划不存在，planId: " + planId);
        }

        Integer taskId = req.getTaskId();
        Integer exists = planTaskMapper.countTaskInPlan(planId, taskId);
        if (exists == null || exists <= 0) {
            throw new IllegalArgumentException("任务不存在或不属于该排课，taskId: " + taskId);
        }

        userTaskRecordMapper.upsertDoneRecord(currentUserId, planId, taskId);

        Integer totalRequired = planTaskMapper.countRequiredTasksByPlanId(planId);
        Integer completedRequired = planTaskMapper.countCompletedRequiredTasksByUserIdAndPlanId(currentUserId, planId);
        int total = totalRequired == null ? 0 : totalRequired;
        int completed = completedRequired == null ? 0 : completedRequired;

        int newRate = 0;
        if (total > 0) {
            newRate = (completed * 100) / total;
        } else {
            newRate = 100;
        }
        int isAllCompleted = (newRate == 100) ? 1 : 0;

        userDailyRecordMapper.upsertSummary(currentUserId, plan.getCampId(), planId, newRate, isAllCompleted);

        eventPublisher.publishEvent(new CampProgressUpdateEvent(this, currentUserId, plan.getCampId()));

        TaskCompleteRespDTO resp = new TaskCompleteRespDTO();
        resp.setPlanId(planId);
        resp.setTaskType(planTaskMapper.selectTaskTypeByTaskId(taskId));
        resp.setCompletionRate(newRate);
        return resp;
    }
    
    @Override
    public CourseDataDTO getCourseData(Integer campId, Long currentUserId) {
        CourseDataDTO dto = new CourseDataDTO();
        java.time.LocalDate today = java.time.LocalDate.now();

        List<CampPlan> allPlans = campPlanMapper.selectCourseScheduleByCampId(campId);
        int totalDays = (allPlans != null) ? allPlans.size() : 0;
        dto.setTotalDays(totalDays);

        List<UserDailyRecord> recordList = userDailyRecordMapper.selectByUserIdAndCampId(currentUserId, campId);
        if (recordList == null) {
            recordList = new ArrayList<>();
        }

        Map<Integer, Integer> rateMap = recordList.stream()
                .collect(Collectors.toMap(
                        UserDailyRecord::getPlanId,
                        r -> r.getCompletionRate() != null ? r.getCompletionRate() : 0,
                        (existing, replacement) -> replacement
                ));

        int completedDays = (int) rateMap.values().stream()
                .filter(rate -> rate == 100)
                .count();
        dto.setCompletedDays(completedDays);

        int overallRate = (totalDays > 0) ? (int) Math.round((completedDays * 100.0) / totalDays) : 0;
        dto.setOverallRate(overallRate);

        List<TrendItemDTO> trends = new ArrayList<>();
        if (allPlans != null && !allPlans.isEmpty()) {
            for (CampPlan plan : allPlans) {
                TrendItemDTO trendItem = new TrendItemDTO();
                trendItem.setDayStr("Day" + plan.getDayIndex());
                trendItem.setDayIndex(plan.getDayIndex());

                int rate = rateMap.getOrDefault(plan.getPlanId(), 0);
                trendItem.setRate(rate);

                java.time.LocalDate planDate = (plan.getPlanDate() == null)
                        ? today
                        : plan.getPlanDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

                if (planDate.isAfter(today)) {
                    trendItem.setStatus("LOCKED");
                    trendItem.setRate(0);
                } else if (rate == 100) {
                    trendItem.setStatus("COMPLETED");
                } else if (rate > 0) {
                    trendItem.setStatus("COMPLETED");
                } else {
                    trendItem.setStatus("MISSED");
                }

                trends.add(trendItem);
            }
        }
        dto.setTrends(trends);

        List<AchievementDTO> achievements = new ArrayList<>();
        
        if (completedDays >= 1) {
            AchievementDTO beginner = new AchievementDTO();
            beginner.setIcon("https://img.icons8.com/color/96/medal2.png");
            beginner.setTitle("初学者");
            beginner.setDesc("完成第一天学习，开启致良知之旅");
            achievements.add(beginner);
        }
        
        if (completedDays >= 3) {
            AchievementDTO progress = new AchievementDTO();
            progress.setIcon("https://img.icons8.com/color/96/warranty.png");
            progress.setTitle("渐入佳境");
            progress.setDesc("累计完成三天学习");
            achievements.add(progress);
        }
        
        if (totalDays > 0 && completedDays == totalDays) {
            AchievementDTO trophy = new AchievementDTO();
            trophy.setIcon("https://img.icons8.com/color/96/trophy.png");
            trophy.setTitle("圆满结业");
            trophy.setDesc("完成全部课程");
            achievements.add(trophy);
        }
        
        dto.setAchievements(achievements);
        
        return dto;
    }
    
    @Override
    public CampInfoDTO getCampInfo(Integer campId) {
        // 1. 调用 Mapper 连表查询获取原始数据
        CampInfoDTO result = campMapper.selectCampInfo(campId);
        
        if (result == null) {
            throw new IllegalArgumentException("营期不存在，campId: " + campId);
        }
        
        // 2. 字段转换处理
        // batch 字段组装："第" + term + "期"
        Integer term = result.getTerm();
        if (term != null) {
            result.setBatch("第" + term + "期");
        } else {
            result.setBatch("");
        }
        
        // description 赋值为 intro
        if (result.getIntro() == null) {
            result.setDescription("");
        }
        
        // participantCount 赋值为 enroll_count（已经在 SQL 中映射）
        if (result.getParticipantCount() == null) {
            result.setParticipantCount(0);
        }
        
        return result;
    }

    @Override
    public Course getCourseDetail(Integer id) {
        Course course = courseMapper.selectCourseById(id);
        if (course == null) {
            throw new IllegalArgumentException("营期不存在，campId: " + id);
        }
        return course;
    }
}
