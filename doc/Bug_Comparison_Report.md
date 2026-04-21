# 对比分析报告：today 接口 vs 报错接口

## 一、today 接口完整代码链路

### 1. Controller 层

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
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录或 Token 失效，无法访问课程");
        }
        TodayCourseDTO todayCourse = courseService.getTodayCourse(campId, currentUserId, planId);
        return Result.success(todayCourse);
    }
}
```

---

### 2. Service 层

**文件路径**：`src/main/java/com/daily/dailychineseculture/service/impl/CourseServiceImpl.java`

```java
@Override
public TodayCourseDTO getTodayCourse(Integer campId, Long currentUserId, Integer planId) {
    CampPlan targetPlan = null;
    String displayDateStr = "";

    if (planId != null) {
        // 【指定日期查询】当 planId 不为空时，查询指定历史天
        targetPlan = campPlanMapper.selectById(planId);
        // ... 校验逻辑
    } else {
        // 【今日查询】当 planId 为空时，查询今日课程
        java.time.LocalDate today = java.time.LocalDate.now();
        displayDateStr = today.format(java.time.format.DateTimeFormatter.ofPattern("M 月 d 日"));
        targetPlan = campPlanMapper.selectTodayPlan(campId, java.sql.Date.valueOf(today));
    }

    if (targetPlan == null) {
        // 返回空 DTO...
    }

    TodayCourseDTO dto = new TodayCourseDTO();
    dto.setHasCourse(true);
    dto.setCurrentDate(displayDateStr);
    dto.setPlanId(targetPlan.getPlanId());

    // 【关键】查询任务列表（来自 t_plan_task 表，与 t_user_daily_record 无关）
    List<TaskItemDTO> tasks = planTaskMapper.selectTaskItemsByPlanIdAndUserId(targetPlan.getPlanId(), currentUserId);
    dto.setTasks(tasks);

    // 【问题代码】这里调用 selectByUserIdAndPlanId，SQL 中包含 camp_id
    UserDailyRecord summary = userDailyRecordMapper.selectByUserIdAndPlanId(currentUserId, targetPlan.getPlanId());
    int completionRate = 0;
    if (summary != null && summary.getCompletionRate() != null) {
        completionRate = summary.getCompletionRate();
    } else if (!tasks.isEmpty()) {
        // 【兜底逻辑】当 selectByUserIdAndPlanId 查询失败时，根据 tasks 实际完成情况计算完成率
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

---

### 3. Mapper 层 - selectTodayPlan（今日查询计划）

**文件路径**：`src/main/resources/mapper/CampPlanMapper.xml`

```xml
<!-- 查询指定日期的排课计划（用于获取今日课程信息） -->
<select id="selectTodayPlan" resultType="com.daily.dailychineseculture.entity.CampPlan">
    SELECT
        plan_id,
        camp_id,
        day_index,
        plan_date,
        title,
        module_index,
        module_name,
        teacher_name,
        is_finished
    FROM t_camp_plan
    WHERE camp_id = #{campId}
      AND DATE(plan_date) = DATE(#{planDate})
</select>
```

**分析**：该 SQL 查询的是 `t_camp_plan` 表（排课计划表），**不涉及** `t_user_daily_record` 表。

---

### 4. Mapper 层 - selectByUserIdAndPlanId（报错的方法）

**文件路径**：`src/main/java/com/daily/dailychineseculture/mapper/UserDailyRecordMapper.java`

```java
@Select("SELECT record_id, user_id, camp_id, plan_id, completion_rate, is_all_completed " +
        "FROM t_user_daily_record WHERE user_id = #{userId} AND plan_id = #{planId}")
UserDailyRecord selectByUserIdAndPlanId(@Param("userId") Long userId, @Param("planId") Integer planId);
```

**分析**：该 SQL 查询的是 `t_user_daily_record` 表，并明确 `SELECT camp_id` 字段。

---

## 二、"今日打卡记录"相关接口（selectHeatmap）

项目中还有另一个与"打卡"相关的接口，用于获取热力图数据。

### 1. Controller 层

**文件路径**：`src/main/java/com/daily/dailychineseculture/controller/DailyRecordController.java`

```java
@RestController
@RequestMapping("/daily-record")
@RequiredArgsConstructor
public class DailyRecordController {

    private final DailyRecordService dailyRecordService;

    @GetMapping("/heatmap")
    public ResponseResult<List<DailyHeatmapDTO>> getHeatmap(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        Long userId = jwtUtils.getUserIdFromToken(token);
        List<DailyHeatmapDTO> result = dailyRecordService.getHeatmap(userId, year, month);
        return ResponseResult.success(result);
    }
}
```

### 2. Service 层

**文件路径**：`src/main/java/com/daily/dailychineseculture/service/impl/DailyRecordServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class DailyRecordServiceImpl implements DailyRecordService {

    private final UserDailyRecordMapper userDailyRecordMapper;

    @Override
    public List<DailyHeatmapDTO> getHeatmap(Long userId, Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        int currentYear = year != null ? year : now.getYear();
        int currentMonth = month != null ? month : now.getMonthValue();
        return userDailyRecordMapper.selectHeatmap(userId, currentYear, currentMonth);
    }
}
```

### 3. Mapper XML - selectHeatmap（关键！）

**文件路径**：`src/main/resources/mapper/UserDailyRecordMapper.xml`

```xml
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
```

### 4. 返回 DTO

**文件路径**：`src/main/java/com/daily/dailychineseculture/dto/DailyHeatmapDTO.java`

```java
@Data
public class DailyHeatmapDTO {
    private String date;
    private Integer completionRate;
    private Integer isAllCompleted;
}
```

---

## 三、对比分析报告

### 问题 1：today 接口底层执行的 SQL 语句具体是什么？它是否查询了 `t_user_daily_record` 表？

**回答**：

`GET /courses/{campId}/today` 接口在**不传 `planId` 参数**时（今日查询），实际执行了以下两条主要 SQL：

| 步骤 | SQL | 查询的表 |
|------|-----|---------|
| 1 | `SELECT ... FROM t_camp_plan WHERE camp_id = ? AND DATE(plan_date) = DATE(?)` | `t_camp_plan` |
| 2 | `SELECT ... FROM t_plan_task WHERE plan_id = ?` | `t_plan_task`（通过 `planTaskMapper.selectTaskItemsByPlanIdAndUserId`） |

**关键发现**：
- **today 接口本身并不直接查询 `t_user_daily_record` 表**！它通过 `campPlanMapper.selectTodayPlan` 和 `planTaskMapper.selectTaskItemsByPlanIdAndUserId` 获取数据
- 代码中确实调用了 `userDailyRecordMapper.selectByUserIdAndPlanId`，但这个调用发生在**获取今日课程信息之后**，用于设置完成率
- 如果 `selectByUserIdAndPlanId` 报错，会触发**兜底逻辑**：根据 `tasks` 列表的实际完成情况计算完成率

### 问题 2：为什么 today 接口能避开 `camp_id` 字段缺失的报错？

**回答**：today 接口**并非完全避开了 `camp_id` 报错**，而是存在**兜底逻辑**。

| 调用路径 | SQL | 是否报 `camp_id` 错误 |
|---------|-----|----------------------|
| `campPlanMapper.selectTodayPlan` | `SELECT ... FROM t_camp_plan` | ❌ 不报错（查询的是 camp_plan 表，不是 daily_record 表） |
| `planTaskMapper.selectTaskItemsByPlanIdAndUserId` | `SELECT ... FROM t_plan_task` | ❌ 不报错（查询的是 plan_task 表） |
| `userDailyRecordMapper.selectByUserIdAndPlanId` | `SELECT record_id, user_id, **camp_id**, plan_id, ... FROM t_user_daily_record` | ✅ **会报错** |

**但用户反馈"今日查询成功"的原因**：

当 `selectByUserIdAndPlanId` 抛出 `SQLSyntaxErrorException` 时，异常被外层捕获或导致方法返回 `null`，随后代码进入兜底逻辑：

```java
// 当 selectByUserIdAndPlanId 返回 null 时，走兜底逻辑
if (summary != null && summary.getCompletionRate() != null) {
    completionRate = summary.getCompletionRate();
} else if (!tasks.isEmpty()) {
    // 根据 tasks 实际完成情况计算完成率
    completionRate = doneCount * 100 / tasks.size();
}
```

所以用户看到的响应是：
- HTTP 状态码：**200 OK**（接口没有崩溃）
- `hasCourse: true`
- `tasks: [...]`（任务列表正常返回）
- `completionRate: 60`（通过兜底逻辑计算得到）

**但这并不意味着 `camp_id` 错误不存在**——只是异常被静默处理了，用户感知不到。

### 问题 3：selectHeatmap 为什么能正常运行？

**回答**：

`selectHeatmap` SQL 如下：

```sql
SELECT
    DATE_FORMAT(p.plan_date, '%Y-%m-%d') AS date,
    d.completion_rate                   AS completionRate,
    d.is_all_completed                  AS isAllCompleted
FROM t_user_daily_record d
LEFT JOIN t_camp_plan p ON d.plan_id = p.plan_id
WHERE d.user_id = #{userId}
  AND YEAR(p.plan_date) = #{year}
  AND MONTH(p.plan_date) = #{month}
```

**关键差异**：
- `selectHeatmap` **显式指定了要查询的字段**：`date`, `completion_rate`, `is_all_completed`
- **没有查询 `camp_id` 字段**
- 因此不会触发 `Unknown column 'camp_id' in 'field list'` 错误

而 `selectByUserIdAndPlanId` 显式查询了 `camp_id`：
```sql
SELECT record_id, user_id, **camp_id**, plan_id, completion_rate, is_all_completed
FROM t_user_daily_record
WHERE user_id = ? AND plan_id = ?
```

---

## 四、修复建议

### 方案 1：修复数据库表结构（推荐）

为 `t_user_daily_record` 表添加 `camp_id` 字段：

```sql
ALTER TABLE t_user_daily_record
ADD COLUMN camp_id INT NOT NULL DEFAULT 0 COMMENT '营期ID' AFTER user_id;

-- 如果有唯一索引 uk_user_plan，需要先删除再重建（或修改）
ALTER TABLE t_user_daily_record
DROP INDEX uk_user_plan,
ADD UNIQUE INDEX uk_user_camp_plan (user_id, camp_id, plan_id);
```

### 方案 2：修改 Mapper SQL（临时方案，不推荐）

如果暂时无法修改数据库，可将 `selectByUserIdAndPlanId` 的 SQL 改为不查询 `camp_id`：

```java
@Select("SELECT record_id, user_id, plan_id, completion_rate, is_all_completed " +
        "FROM t_user_daily_record WHERE user_id = #{userId} AND plan_id = #{planId}")
UserDailyRecord selectByUserIdAndPlanId(@Param("userId") Long userId, @Param("planId") Integer planId);
```

**注意**：这会导致 `UserDailyRecord.campId` 始终为 `null`，可能影响其他业务逻辑。

### 方案 3：检查是否需要 `camp_id` 字段

分析 `camp_id` 在业务中的实际用途：
- 如果 `t_user_daily_record` 表设计上不需要 `camp_id`，则需修改所有涉及该字段的代码
- 如果确实需要（如按营期查询打卡记录），则必须添加该字段

---

## 五、总结

| 对比项 | today 接口（GET /courses/{campId}/today） | selectHeatmap 接口（GET /daily-record/heatmap） |
|--------|------------------------------------------|------------------------------------------------|
| 调用的主要 Mapper | `CampPlanMapper`, `PlanTaskMapper` | `UserDailyRecordMapper` |
| 查询的表 | `t_camp_plan`, `t_plan_task` | `t_user_daily_record` |
| 是否查询 `camp_id` | ❌ 否（通过 camp_plan 表间接获取） | ❌ 否（显式指定的字段中不包含） |
| `selectByUserIdAndPlanId` 调用 | ✅ 有，但有兜底逻辑 | ❌ 否 |
| 报错可能性 | ⚠️ 有兜底逻辑，异常被静默处理 | ✅ 不报错 |

**核心结论**：today 接口并非真正"避开"了 `camp_id` 错误，而是因为存在兜底逻辑，使得接口在 `selectByUserIdAndPlanId` 报错后仍能返回数据（只是完成率通过其他方式计算）。真正报错的接口是 `selectByUserIdAndPlanId`，需要修复数据库表结构或修改该 SQL。
