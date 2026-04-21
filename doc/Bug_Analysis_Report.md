# Bug 分析报告：指定日期查询用户打卡记录失败

## 错误信息

```
java.sql.SQLSyntaxErrorException: Unknown column 'camp_id' in 'field list'
```

**报错定位**：`com.daily.dailychineseculture.mapper.UserDailyRecordMapper.selectByUserIdAndPlanId`

**执行 SQL**：
```sql
SELECT record_id, user_id, camp_id, plan_id, completion_rate, is_all_completed
FROM t_user_daily_record
WHERE user_id = ? AND plan_id = ?
```

---

## 1. Entity（实体类）

**文件路径**：`src/main/java/com/daily/dailychineseculture/entity/UserDailyRecord.java`

```java
package com.daily.dailychineseculture.entity;

import lombok.Data;

@Data
public class UserDailyRecord {
    private Integer recordId;
    private Long userId;
    private Integer campId;       // 营期 ID
    private Integer planId;
    private Integer completionRate;  // 完成率 (0-100)
    private Integer isAllCompleted; // 是否全部完成：0-未完成，1-已完成
}
```

**分析**：实体类包含 `campId` 字段。

---

## 2. Mapper（数据访问层）

### 2.1 Mapper 接口

**文件路径**：`src/main/java/com/daily/dailychineseculture/mapper/UserDailyRecordMapper.java`

```java
package com.daily.dailychineseculture.mapper;

@Mapper
public interface UserDailyRecordMapper {

    @Select("SELECT record_id, user_id, camp_id, plan_id, completion_rate, is_all_completed " +
            "FROM t_user_daily_record WHERE record_id = #{recordId}")
    UserDailyRecord selectById(Integer recordId);

    @Select("SELECT record_id, user_id, camp_id, plan_id, completion_rate, is_all_completed " +
            "FROM t_user_daily_record WHERE user_id = #{userId} AND plan_id = #{planId}")
    UserDailyRecord selectByUserIdAndPlanId(@Param("userId") Long userId, @Param("planId") Integer planId);

    @Select("SELECT record_id, user_id, camp_id, plan_id, completion_rate, is_all_completed " +
            "FROM t_user_daily_record WHERE user_id = #{userId} AND camp_id = #{campId}")
    List<UserDailyRecord> selectByUserIdAndCampId(@Param("userId") Long userId, @Param("campId") Integer campId);

    @Insert("INSERT INTO t_user_daily_record(user_id, camp_id, plan_id, completion_rate, is_all_completed) " +
            "VALUES(#{userId}, #{campId}, #{planId}, #{completionRate}, #{isAllCompleted}) " +
            "ON DUPLICATE KEY UPDATE completion_rate = #{completionRate}, is_all_completed = #{isAllCompleted}")
    int upsertSummary(@Param("userId") Long userId, @Param("campId") Integer campId,
                      @Param("planId") Integer planId, @Param("completionRate") Integer completionRate,
                      @Param("isAllCompleted") Integer isAllCompleted);

    List<DailyHeatmapDTO> selectHeatmap(@Param("userId") Long userId,
                                         @Param("year") Integer year, @Param("month") Integer month);
}
```

**分析**：Mapper 接口中所有 SQL 语句都包含 `camp_id` 字段。

### 2.2 MyBatis XML 映射文件

**文件路径**：`src/main/resources/mapper/UserDailyRecordMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.daily.dailychineseculture.mapper.UserDailyRecordMapper">

    <resultMap id="UserDailyRecordResult" type="com.daily.dailychineseculture.entity.UserDailyRecord">
        <id property="recordId" column="record_id"/>
        <result property="userId" column="user_id"/>
        <result property="campId" column="camp_id"/>
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

**分析**：
- `resultMap` 中定义了 `camp_id` 字段映射
- `selectHeatmap` 方法（用于"今日打卡"查询）**不查询 `camp_id`**，只查询 `completion_rate` 和 `is_all_completed`
- 这就是为什么"今日打卡记录查询成功"的原因

---

## 3. Service（业务逻辑层）

### 3.1 Service 接口

**文件路径**：`src/main/java/com/daily/dailychineseculture/service/CourseService.java`

```java
public interface CourseService {
    TodayCourseDTO getTodayCourse(Integer campId, Long userId, Integer planId);
    TaskCompleteRespDTO completeTask(Integer planId, TaskCompleteReqDTO req, Long userId);
    CourseDataDTO getCourseData(Integer campId, Long userId);
    // ... 其他方法
}
```

### 3.2 Service 实现

**文件路径**：`src/main/java/com/daily/dailychineseculture/service/impl/CourseServiceImpl.java`

**关键方法 `getTodayCourse`**（对应"指定日期查询"业务）：

```java
@Override
public TodayCourseDTO getTodayCourse(Integer campId, Long currentUserId, Integer planId) {
    CampPlan targetPlan = null;
    String displayDateStr = "";

    if (planId != null) {
        // 【指定日期查询】当 planId 不为空时，查询指定历史天
        targetPlan = campPlanMapper.selectById(planId);
        // ...
    } else {
        // 【今日查询】当 planId 为空时，查询今日课程
        java.time.LocalDate today = java.time.LocalDate.now();
        displayDateStr = today.format(java.time.format.DateTimeFormatter.ofPattern("M 月 d 日"));
        targetPlan = campPlanMapper.selectTodayPlan(campId, java.sql.Date.valueOf(today));
    }
    // ...

    TodayCourseDTO dto = new TodayCourseDTO();
    dto.setHasCourse(true);
    dto.setCurrentDate(displayDateStr);
    dto.setPlanId(targetPlan.getPlanId());

    List<TaskItemDTO> tasks = planTaskMapper.selectTaskItemsByPlanIdAndUserId(targetPlan.getPlanId(), currentUserId);
    dto.setTasks(tasks);

    // 【问题所在】这里调用 selectByUserIdAndPlanId，SQL 中包含 camp_id
    UserDailyRecord summary = userDailyRecordMapper.selectByUserIdAndPlanId(currentUserId, targetPlan.getPlanId());
    int completionRate = 0;
    if (summary != null && summary.getCompletionRate() != null) {
        completionRate = summary.getCompletionRate();
    } else if (!tasks.isEmpty()) {
        // 兜底逻辑：根据 tasks 计算完成率
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
```

**关键方法 `completeTask`**（对应"完成任务打卡"业务）：

```java
@Override
@Transactional(rollbackFor = Exception.class)
public TaskCompleteRespDTO completeTask(Integer planId, TaskCompleteReqDTO req, Long currentUserId) {
    // ...
    // 使用 upsertSummary 写入记录（INSERT 时包含 camp_id）
    userDailyRecordMapper.upsertSummary(currentUserId, plan.getCampId(), planId, newRate, isAllCompleted);
    // ...
}
```

---

## 4. Controller（控制层）

**文件路径**：`src/main/java/com/daily/dailychineseculture/controller/AppCourseController.java`

```java
@RestController
@RequestMapping("/courses")
public class AppCourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 获取指定营期的今日课程（支持时光机模式）
     * GET /courses/{campId}/today
     *
     * @param campId 营期 ID
     * @param planId 排课计划 ID（可选，传入时查询指定历史天）
     */
    @GetMapping("/{campId}/today")
    public Result<TodayCourseDTO> getTodayCourse(
            @PathVariable Integer campId,
            @RequestParam(required = false) Integer planId,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        TodayCourseDTO todayCourse = courseService.getTodayCourse(campId, currentUserId, planId);
        return Result.success(todayCourse);
    }

    /**
     * 完成任务打卡
     * POST /courses/plan/{planId}/task/complete
     */
    @PostMapping("/plan/{planId}/task/complete")
    public Result<TaskCompleteRespDTO> completeTask(@PathVariable Integer planId,
                                                     @RequestBody TaskCompleteReqDTO req,
                                                     HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        TaskCompleteRespDTO resp = courseService.completeTask(planId, req, currentUserId);
        return Result.success(resp);
    }
}
```

**接口对比**：
| 接口 | 路由 | planId | 调用方法 |
|------|------|--------|----------|
| 今日课程 | `GET /courses/{campId}/today` | 不传 | `getTodayCourse(campId, userId, null)` |
| 指定日期课程 | `GET /courses/{campId}/today?planId=xxx` | 传入 | `getTodayCourse(campId, userId, planId)` |

---

## 5. 数据库 DDL

项目中**未找到** `t_user_daily_record` 表的建表语句（无 `.sql` 文件、无 Flyway/Liquibase 迁移文件）。

---

## 6. Bug 原因初步分析

### 问题根因

**数据库表 `t_user_daily_record` 中缺少 `camp_id` 字段**，但 Java 代码（Entity、Mapper SQL、XML resultMap）中都在使用该字段。

### 为什么"今日打卡记录查询成功"但"指定日期查询失败"？

1. **`getTodayCourse` 方法在没有传 `planId` 时（今日查询）**：
   - 仍然会调用 `userDailyRecordMapper.selectByUserIdAndPlanId(currentUserId, targetPlan.getPlanId())`
   - 但如果数据库报错，通常会抛出 `SQLSyntaxErrorException`
   - 用户反馈"今日查询成功"可能是指业务逻辑走了兜底路径（见下方说明）

2. **可能的兜底逻辑**：
   - 查看 `CourseServiceImpl.getTodayCourse` 代码，当 `selectByUserIdAndPlanId` 返回 `null` 时（即查询失败被捕获或返回空），会进入兜底逻辑：
     ```java
     if (summary != null && summary.getCompletionRate() != null) {
         completionRate = summary.getCompletionRate();
     } else if (!tasks.isEmpty()) {
         // 根据 tasks 实际完成情况计算完成率
         completionRate = doneCount * 100 / tasks.size();
     }
     ```
   - 这导致即使 `selectByUserIdAndPlanId` 报错，用户仍然可能看到返回数据（只是完成率通过其他方式计算）

3. **`selectHeatmap`（热力图/今日打卡）为什么不报错？**
   - 查看 XML 中的 `selectHeatmap` SQL：
     ```sql
     SELECT
         DATE_FORMAT(p.plan_date, '%Y-%m-%d') AS date,
         d.completion_rate                   AS completionRate,
         d.is_all_completed                  AS isAllCompleted
     FROM t_user_daily_record d
     LEFT JOIN t_camp_plan p ON d.plan_id = p.plan_id
     ```
   - 该 SQL **不查询 `camp_id` 字段**，所以不会触发 `Unknown column 'camp_id'` 错误

### 修复建议

1. **检查并修复数据库表结构**：为 `t_user_daily_record` 表添加 `camp_id` 字段
   ```sql
   ALTER TABLE t_user_daily_record ADD COLUMN camp_id INT NOT NULL COMMENT '营期ID';
   ```

2. **或者**：如果该字段确实不需要，可修改代码移除对 `camp_id` 的依赖（但需评估对业务的影响）

3. **建议排查方向**：
   - 查看数据库实际表结构，确认 `camp_id` 字段是否存在
   - 检查是否有数据库迁移脚本被遗漏
   - 确认测试环境与生产环境的数据库结构是否一致

---

## 附录：关键代码路径汇总

| 模块 | 文件路径 |
|------|----------|
| Entity | `src/main/java/com/daily/dailychineseculture/entity/UserDailyRecord.java` |
| Mapper 接口 | `src/main/java/com/daily/dailychineseculture/mapper/UserDailyRecordMapper.java` |
| Mapper XML | `src/main/resources/mapper/UserDailyRecordMapper.xml` |
| Service 接口 | `src/main/java/com/daily/dailychineseculture/service/CourseService.java` |
| Service 实现 | `src/main/java/com/daily/dailychineseculture/service/impl/CourseServiceImpl.java` |
| Controller | `src/main/java/com/daily/dailychineseculture/controller/AppCourseController.java` |
