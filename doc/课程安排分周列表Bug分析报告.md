# 课程安排分周列表 Bug 分析报告

## 一、接口入口

**Controller**：`AppCourseController.java`

```java
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
}
```

---

## 二、Service 层

**接口**：`CourseService.java`（第 38 行）

```java
List<CampScheduleDTO> getCourseSchedule(Integer campId);
```

**实现**：`CourseServiceImpl.java`（第 87–125 行）

```java
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
 * 最终返回："第" + chineseNumber + "周：" + moduleName
 */
private String buildWeekName(Integer moduleIndex, String moduleName) {
    String[] chineseNumbers = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};

    String chineseNumber;
    if (moduleIndex >= 1 && moduleIndex <= 10) {
        chineseNumber = chineseNumbers[moduleIndex];
    } else {
        chineseNumber = moduleIndex.toString();
    }

    return "第" + chineseNumber + "周：" + moduleName;
}
```

---

## 三、Mapper / SQL 层

**Mapper 接口**：`CampPlanMapper.java`（第 81 行）

```java
List<CampPlan> selectCourseScheduleByCampId(@Param("campId") Integer campId);
```

**XML SQL**：`CampPlanMapper.xml`（第 73–88 行）

```xml
<!-- 根据营期 ID 查询课程安排（按天排序） -->
<select id="selectCourseScheduleByCampId" resultType="com.daily.dailychineseculture.entity.CampPlan">
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
    ORDER BY day_index ASC
</select>
```

---

## 四、关键 DTO

**CampScheduleDTO.java**

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampScheduleDTO {
    /**
     * 模块索引（第几周，对应 t_camp_plan.module_index）
     */
    private Integer moduleIndex;

    /**
     * 模块名称（后端拼接后的完整字符串，如 "第一周：基础认知"）
     */
    private String moduleName;

    /**
     * 该模块下的所有课程计划
     */
    private List<PlanItemDTO> plans;
}
```

**PlanItemDTO.java**

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanItemDTO {
    private Integer planId;      // 计划 ID
    private Integer dayIndex;   // 第几天
    private String title;       // 课程标题
    private String teacherName; // 讲师姓名
}
```

---

## 五、数据实体

**CampPlan.java**

```java
@Data
@Alias("CampPlan")
public class CampPlan {
    private Integer planId;       // 计划 ID
    private Integer campId;       // 营期 ID
    private Integer dayIndex;     // 第几天
    private Date planDate;        // 具体日期
    private String title;         // 导读标题
    private Integer moduleIndex;  // 模块索引（第几周）
    private String moduleName;    // 模块名称
    private String teacherName;   // 讲师姓名
    private Integer isFinished;   // 是否完成：0-未完成，1-已完成
}
```

---

## 六、字段映射关系

| 数据库字段（t_camp_plan） | Java 实体属性 | 说明 |
|---|---|---|
| `plan_id` | `planId` | 计划 ID |
| `camp_id` | `campId` | 营期 ID |
| `day_index` | `dayIndex` | 第几天 |
| `module_index` | `moduleIndex` | **关键字段：第几周索引** |
| `module_name` | `moduleName` | **关键字段：模块/周名称** |

---

## 七、Bug 根因分析

### Bug 1：第0周: null

**原因**：数据库中存在 `module_index = 0` 或 `NULL` 的记录。

`buildWeekName` 方法中，当 `moduleIndex = 0` 时：

```java
String[] chineseNumbers = {"", "一", "二", "三", ...};
// chineseNumbers[0] = ""（空字符串）
return "第" + "" + "周：" + moduleName;
// 输出："第0周：" + null = "第0周: null"
```

**修复建议**：在 `buildWeekName` 开头做 null/0 安全校验：

```java
private String buildWeekName(Integer moduleIndex, String moduleName) {
    if (moduleIndex == null || moduleIndex <= 0) {
        moduleIndex = 1; // 兜底为第1周
    }
    if (moduleName == null) {
        moduleName = ""; // 防止 null 拼接
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
```

### Bug 2：第一周: 第一周:

**原因**：数据库中 `module_name` 字段已经包含了完整的前缀文本，例如 `"第一周：基础认知"`。Service 层在此基础上又拼接了一次：

```java
String moduleName = plans.get(0).getModuleName(); // "第一周：基础认知"
String fullModuleName = buildWeekName(moduleIndex, moduleName);
// buildWeekName 内部再次拼接："第" + "一" + "周：" + "第一周：基础认知"
// 输出："第一周：第一周：基础认知"
```

**修复建议**：确认 `module_name` 字段的语义：
- 如果 `module_name` 存的是纯主题（如 `"基础认知"`），则 Service 层拼接逻辑正确，无需修改。
- 如果 `module_name` 存的已经是完整字符串（如 `"第一周：基础认知"`），则 Service 层不应再调用 `buildWeekName`，直接使用 `moduleName` 即可：

```java
String moduleName = plans.get(0).getModuleName();
String fullModuleName = (moduleName != null && !moduleName.startsWith("第"))
    ? buildWeekName(moduleIndex, moduleName)
    : moduleName; // 已经是完整周名称，直接使用
```

---

## 八、修复方案汇总

| 问题 | 根因 | 修复位置 | 修复动作 |
|---|---|---|---|
| 第0周: null | `module_index = 0` 且 `moduleName` 为 null | `buildWeekName()` | 开头加 null/0 校验兜底 |
| 第一周: 第一周: | `module_name` 已是完整字符串，重复拼接 | `getCourseSchedule()` 第 102 行 | 加判断：已有"第"字则跳过拼接 |
| 判空缺失 | `plans.get(0).getModuleName()` 可能 NPE | `getCourseSchedule()` 第 102 行 | 加 null 检查 |
