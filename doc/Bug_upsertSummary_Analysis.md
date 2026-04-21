# Bug 分析报告：任务打卡完成功能 upsertSummary 报错

## 错误信息

```
Field 'camp_id' doesn't have a default value
```

**报错位置**：`com.daily.dailychineseculture.mapper.UserDailyRecordMapper.upsertSummary`

**报错 SQL**：
```sql
INSERT INTO t_user_daily_record(user_id, plan_id, completion_rate, is_all_completed)
VALUES(?, ?, ?, ?)
ON DUPLICATE KEY UPDATE completion_rate = ?, is_all_completed = ?
```

**核心矛盾**：SQL 语句中缺少 `camp_id` 字段，但数据库表结构要求该字段必填（`NOT NULL`）。

---

## 1. Controller 层

**文件路径**：`src/main/java/com/daily/dailychineseculture/controller/AppCourseController.java`

```java
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
     */
    @GetMapping("/{campId}/schedule")
    public Result<List<CampScheduleDTO>> getCourseSchedule(@PathVariable Integer campId) {
        List<CampScheduleDTO> scheduleList = courseService.getCourseSchedule(campId);
        return Result.success(scheduleList);
    }

    /**
     * 获取指定营期的今日课程（支持时光机模式）
     * GET /courses/{campId}/today
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
    public Result<TaskCompleteRespDTO> completeTask(@PathVariable Integer planId,
                                                   @RequestBody TaskCompleteReqDTO req,
                                                   HttpServletRequest request) {
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
     */
    @GetMapping("/{campId}/info")
    public Result<CampInfoDTO> getCampInfo(@PathVariable Integer campId) {
        CampInfoDTO info = courseService.getCampInfo(campId);
        return Result.success(info);
    }
}
```

---

## 2. Service 接口层

**文件路径**：`src/main/java/com/daily/dailychineseculture/service/CourseService.java`

```java
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
 */
public interface CourseService {

    /**
     * 根据用户ID和标签类型获取我的课程列表
     */
    List<MyCourseVO> getMyCourses(Long userId, Integer tabType);

    /**
     * 获取指定营期的课程安排目录
     */
    List<CampScheduleDTO> getCourseSchedule(Integer campId);

    /**
     * 获取指定营期的今日课程（微信小程序端）
     * 支持时光机模式：传入 planId 时查询指定历史天
     */
    TodayCourseDTO getTodayCourse(Integer campId, Long userId, Integer planId);

    /**
     * 完成任务打卡并返回最新进度（微信小程序端）
     */
    TaskCompleteRespDTO completeTask(Integer planId, TaskCompleteReqDTO req, Long userId);

    /**
     * 获取课程数据看板（微信小程序端）
     */
    CourseDataDTO getCourseData(Integer campId, Long userId);

    /**
     * 获取营期详情信息（移动端课程详情页顶部信息栏）
     */
    CampInfoDTO getCampInfo(Integer campId);

    Course getCourseDetail(Integer id);
}
```

---

## 3. Service 实现层（关键：completeTask 方法）

**文件路径**：`src/main/java/com/daily/dailychineseculture/service/impl/CourseServiceImpl.java`

```java
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
        if (userId == null || tabType == null) {
            throw new IllegalArgumentException("用户 ID 和标签类型不能为空");
        }
        if (tabType < 1 || tabType > 3) {
            throw new IllegalArgumentException("标签类型必须为 1、2 或 3");
        }
        return myCourseMapper.selectMyCourses(userId, tabType);
    }

    @Override
    public List<CampScheduleDTO> getCourseSchedule(Integer campId) {
        List<CampPlan> allPlans = campPlanMapper.selectCourseScheduleByCampId(campId);
        if (allPlans == null || allPlans.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Integer, List<CampPlan>> groupedByModule = allPlans.stream()
                .collect(Collectors.groupingBy(CampPlan::getModuleIndex));
        List<CampScheduleDTO> scheduleList = new ArrayList<>();
        for (Map.Entry<Integer, List<CampPlan>> entry : groupedByModule.entrySet()) {
            Integer moduleIndex = entry.getKey();
            List<CampPlan> plans = entry.getValue();
            String rawModuleName = plans.get(0).getModuleName();
            String fullModuleName = buildWeekName(moduleIndex, rawModuleName);
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
        scheduleList.sort((a, b) -> a.getModuleIndex().compareTo(b.getModuleIndex()));
        return scheduleList;
    }

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
     * 【关键方法】完成任务打卡
     */
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

        // 【问题代码】这里调用 upsertSummary，但移除了 campId 参数
        // 数据库要求 camp_id 字段必填，因此报错
        userDailyRecordMapper.upsertSummary(currentUserId, planId, newRate, isAllCompleted);

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
        CampInfoDTO result = campMapper.selectCampInfo(campId);
        if (result == null) {
            throw new IllegalArgumentException("营期不存在，campId: " + campId);
        }
        Integer term = result.getTerm();
        if (term != null) {
            result.setBatch("第" + term + "期");
        } else {
            result.setBatch("");
        }
        if (result.getIntro() == null) {
            result.setDescription("");
        }
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
```

---

## 4. Mapper 接口层

**文件路径**：`src/main/java/com/daily/dailychineseculture/mapper/UserDailyRecordMapper.java`

```java
package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.UserDailyRecord;
import com.daily.dailychineseculture.dto.DailyHeatmapDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户每日学习记录 Mapper
 */
@Mapper
public interface UserDailyRecordMapper {

    /**
     * 根据 ID 查询记录
     */
    @Select("SELECT record_id, user_id, plan_id, completion_rate, is_all_completed FROM t_user_daily_record WHERE record_id = #{recordId}")
    UserDailyRecord selectById(Integer recordId);

    /**
     * 根据用户 ID 和计划 ID 查询记录（user_id + plan_id 联合唯一）
     */
    @Select("SELECT record_id, user_id, plan_id, completion_rate, is_all_completed FROM t_user_daily_record WHERE user_id = #{userId} AND plan_id = #{planId}")
    UserDailyRecord selectByUserIdAndPlanId(@Param("userId") Long userId, @Param("planId") Integer planId);

    /**
     * 根据用户 ID 和营期 ID 查询所有打卡记录
     * 由于 t_user_daily_record 表没有 camp_id 字段，通过 JOIN t_camp_plan 表来实现
     */
    @Select("SELECT d.record_id, d.user_id, d.plan_id, d.completion_rate, d.is_all_completed " +
            "FROM t_user_daily_record d " +
            "JOIN t_camp_plan p ON d.plan_id = p.plan_id " +
            "WHERE d.user_id = #{userId} AND p.camp_id = #{campId}")
    List<UserDailyRecord> selectByUserIdAndCampId(@Param("userId") Long userId, @Param("campId") Integer campId);

    /**
     * 插入或更新汇总记录
     * 【问题】当前 SQL 没有包含 camp_id，但数据库要求该字段必填
     */
    @Insert("INSERT INTO t_user_daily_record(user_id, plan_id, completion_rate, is_all_completed) " +
            "VALUES(#{userId}, #{planId}, #{completionRate}, #{isAllCompleted}) " +
            "ON DUPLICATE KEY UPDATE completion_rate = #{completionRate}, is_all_completed = #{isAllCompleted}")
    int upsertSummary(@Param("userId") Long userId,
                      @Param("planId") Integer planId,
                      @Param("completionRate") Integer completionRate,
                      @Param("isAllCompleted") Integer isAllCompleted);

    List<DailyHeatmapDTO> selectHeatmap(@Param("userId") Long userId,
                                       @Param("year") Integer year,
                                       @Param("month") Integer month);
}
```

---

## 5. Mapper XML 配置

**文件路径**：`src/main/resources/mapper/UserDailyRecordMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.daily.dailychineseculture.mapper.UserDailyRecordMapper">

    <resultMap id="UserDailyRecordResult" type="com.daily.dailychineseculture.entity.UserDailyRecord">
        <id property="recordId" column="record_id"/>
        <result property="userId" column="user_id"/>
        <result property="planId" column="plan_id"/>
        <result property="completionRate" column="completion_rate"/>
        <result property="isAllCompleted" column="is_all_completed"/>
    </resultMap>

    <select id="selectHeatmap" resultType="com.daily.dailychineseculture.dto.DailyHeatmapDTO">
        SELECT
            DATE_FORMAT(p.plan_date, '%Y-%m-%d') AS date,
            d.completion_rate                   AS completionRate,
            d.is_all_completed                  AS isAllCompleted
        FROM t_user_daily_record d
        LEFT JOIN t_camp_plan p ON d.plan_id = p.plan_id
        WHERE d.user_id = #{userId}
          AND YEAR(p.plan_date) = #{year}
          AND MONTH(p.plan_date) = #{month}
        ORDER BY p.plan_date ASC
    </select>

</mapper>
```

---

## 6. Entity 实体类

**文件路径**：`src/main/java/com/daily/dailychineseculture/entity/UserDailyRecord.java`

```java
package com.daily.dailychineseculture.entity;

import lombok.Data;

/**
 * 用户每日学习记录实体类
 * 对应数据库表：t_user_daily_record
 */
@Data
public class UserDailyRecord {
    /**
     * 记录 ID (主键)
     */
    private Integer recordId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 计划 ID
     */
    private Integer planId;

    /**
     * 完成率 (0-100)
     */
    private Integer completionRate;

    /**
     * 是否全部完成：0-未完成，1-已完成
     */
    private Integer isAllCompleted;
}
```

---

## 7. 数据库 DDL（来自 backend_context.md）

**文件路径**：`backend_context.md`（项目文档）

```sql
CREATE TABLE t_user_daily_record (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    camp_id INT NOT NULL,
    plan_id INT NOT NULL,
    is_read_done TINYINT DEFAULT 0,
    is_video_done TINYINT DEFAULT 0,
    is_homework_done TINYINT DEFAULT 0,
    is_extra1_done TINYINT DEFAULT 0,
    is_extra2_done TINYINT DEFAULT 0,
    completion_rate INT DEFAULT 0,
    UNIQUE KEY uk_user_plan (user_id, plan_id)
);
```

**关键**：数据库 DDL 中 `camp_id INT NOT NULL`，表示该字段**必填**。

---

## 8. Bug 根因分析

### 问题演变过程

1. **最初问题**：代码中 `selectByUserIdAndPlanId` 查询了 `camp_id` 字段，但数据库表中没有该字段，导致 `Unknown column 'camp_id' in 'field list'` 错误。

2. **第一次修复**：根据用户要求"不修改数据库表结构"，移除了代码中所有 `camp_id` 相关内容：
   - 删除了 `UserDailyRecord.campId` 字段
   - 删除了 `upsertSummary` 方法中的 `campId` 参数
   - 修改了 `selectByUserIdAndCampId` 为 JOIN 查询

3. **新问题产生**：`upsertSummary` 方法移除了 `campId` 参数后，INSERT SQL 变为：
   ```sql
   INSERT INTO t_user_daily_record(user_id, plan_id, completion_rate, is_all_completed)
   VALUES(?, ?, ?, ?)
   ```
   但数据库 DDL 要求 `camp_id INT NOT NULL`，导致报错：
   ```
   Field 'camp_id' doesn't have a default value
   ```

### 矛盾点

| 位置 | 要求 |
|------|------|
| 数据库 DDL | `camp_id INT NOT NULL`（必填） |
| 当前代码 | `INSERT` 语句不包含 `camp_id` |

---

## 9. 修复建议

### 方案对比

| 方案 | 描述 | 优点 | 缺点 |
|------|------|------|------|
| **方案 A** | 修改数据库让 `camp_id` 允许为空 | 代码改动小 | 违反原始 DDL 设计，可能影响其他依赖 `camp_id` 的业务 |
| **方案 B** | 在代码中重新补回 `camp_id` | 符合原始设计，保持数据完整性 | 需要修改多个文件 |

### 推荐方案：方案 B - 在代码中重新补回 `camp_id`

**理由**：
1. 数据库 DDL 已经明确定义 `camp_id INT NOT NULL`，说明设计意图是需要该字段的
2. `t_user_daily_record` 表通过 `plan_id` 与 `t_camp_plan` 表关联，`camp_id` 是冗余字段但业务上需要
3. `completeTask` 方法中 `CampPlan` 对象有 `getCampId()` 方法，可以获取到该值

### 具体修改步骤

**Step 1: 恢复 Entity 中的 campId 字段**

```java
// UserDailyRecord.java
private Integer campId;  // 恢复此字段
```

**Step 2: 恢复 Mapper XML 中的 resultMap 映射**

```xml
<resultMap id="UserDailyRecordResult" type="com.daily.dailychineseculture.entity.UserDailyRecord">
    <id property="recordId" column="record_id"/>
    <result property="userId" column="user_id"/>
    <result property="campId" column="camp_id"/>  <!-- 恢复此行 -->
    <result property="planId" column="plan_id"/>
    <result property="completionRate" column="completion_rate"/>
    <result property="isAllCompleted" column="is_all_completed"/>
</resultMap>
```

**Step 3: 恢复 Mapper 接口中的 SQL（selectById, selectByUserIdAndPlanId）**

```java
@Select("SELECT record_id, user_id, camp_id, plan_id, completion_rate, is_all_completed " +
        "FROM t_user_daily_record WHERE record_id = #{recordId}")
UserDailyRecord selectById(Integer recordId);

@Select("SELECT record_id, user_id, camp_id, plan_id, completion_rate, is_all_completed " +
        "FROM t_user_daily_record WHERE user_id = #{userId} AND plan_id = #{planId}")
UserDailyRecord selectByUserIdAndPlanId(@Param("userId") Long userId, @Param("planId") Integer planId);
```

**Step 4: 恢复 upsertSummary 方法的 campId 参数**

```java
@Insert("INSERT INTO t_user_daily_record(user_id, camp_id, plan_id, completion_rate, is_all_completed) " +
        "VALUES(#{userId}, #{campId}, #{planId}, #{completionRate}, #{isAllCompleted}) " +
        "ON DUPLICATE KEY UPDATE completion_rate = #{completionRate}, is_all_completed = #{isAllCompleted}")
int upsertSummary(@Param("userId") Long userId,
                  @Param("campId") Integer campId,
                  @Param("planId") Integer planId,
                  @Param("completionRate") Integer completionRate,
                  @Param("isAllCompleted") Integer isAllCompleted);
```

**Step 5: 恢复 Service 中的调用（保留 plan.getCampId()）**

```java
// CourseServiceImpl.java - completeTask 方法中
userDailyRecordMapper.upsertSummary(currentUserId, plan.getCampId(), planId, newRate, isAllCompleted);
```

**Step 6: 恢复 selectByUserIdAndCampId 的 SQL（不需要 JOIN 了）**

```java
@Select("SELECT record_id, user_id, camp_id, plan_id, completion_rate, is_all_completed " +
        "FROM t_user_daily_record WHERE user_id = #{userId} AND camp_id = #{campId}")
List<UserDailyRecord> selectByUserIdAndCampId(@Param("userId") Long userId, @Param("campId") Integer campId);
```

---

## 10. 总结

**核心问题**：之前的修复方案不完整，只移除了 `camp_id` 的查询部分，但没有移除 `camp_id` 的写入部分，导致 INSERT 时违反数据库约束。

**推荐修复**：恢复 `camp_id` 字段的全部代码逻辑，因为：
1. 数据库 DDL 已经定义了 `camp_id INT NOT NULL`
2. `CampPlan` 对象在 `completeTask` 方法中已经包含了 `campId` 信息
3. 这是最小改动、最稳妥的修复方案
