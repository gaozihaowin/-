# 小程序端课程详情页 - task 数据供应链路盘点

---

## 1. 小程序端查询接口

**文件：** `AppCourseController.java`

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
     * @param request HTTP 请求（用于获取登录用户 ID）
     * @return 今日/历史课程信息
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

**路径：** `GET /courses/{campId}/today`（无 `/api/` 前缀）

---

## 2. DTO 结构

**文件：** `TodayCourseDTO.java`

```java
package com.daily.dailychineseculture.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodayCourseDTO {
    private Boolean hasCourse;
    private String currentDate;
    private Integer planId;
    private Integer completionRate;
    private List<TaskItemDTO> tasks;
}
```

**文件：** `TaskItemDTO.java`

```java
package com.daily.dailychineseculture.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskItemDTO {
    private Integer taskId;
    private String taskType;
    private String title;
    private String subtitle;
    private Integer isRequired;
    private Integer isDone;
}
```

**⚠️ 重点确认：**
- `task_url` 字段：**不存在**于 `TaskItemDTO` 中
- `task_type` 字段：**存在**，但 DTO 仅声明为 `String taskType`，无大写约束

---

## 3. Mapper XML 查询 SQL

**文件：** `PlanTaskMapper.xml`

```xml
<!-- 根据排课 ID 和用户 ID 查询任务列表（C 端使用） -->
<select id="selectTaskItemsByPlanIdAndUserId" resultType="com.daily.dailychineseculture.dto.TaskItemDTO">
    SELECT
        t.task_id AS taskId,
        t.task_type AS taskType,
        t.task_name AS title,
        t.task_desc AS subtitle,
        t.is_required AS isRequired,
        IF(r.record_id IS NOT NULL, 1, 0) AS isDone
    FROM t_plan_task t
    LEFT JOIN t_user_task_record r ON t.task_id = r.task_id
        AND r.user_id = #{userId}
        AND r.is_done = 1
    WHERE t.plan_id = #{planId}
      AND t.is_deleted = 0
    ORDER BY t.sort_order ASC
</select>
```

**⚠️ 重点确认：**
- `task_type` 字段：**未做** `UPPER()` 处理，直接透传数据库原始值
- `task_url` 字段：**未查询**，SQL 中无此字段
- `sort_order` 字段：**存在**，用于 `ORDER BY`

---

## 4. t_plan_task 表结构

> ⚠️ **无法执行 DESCRIBE**，本机未安装 mysql CLI。请手动执行以下 SQL 并确认：
>
> ```sql
> DESCRIBE t_plan_task;
> ```
>
> 预期字段列表：
> - `task_id` (主键)
> - `plan_id` (外键)
> - `task_type` (varchar)
> - `task_name` (varchar)
> - `task_desc` (varchar)
> - `task_url` (varchar)
> - `duration` (int)
> - `is_required` (int)
> - `sort_order` (int)
> - `is_deleted` (int)

---

## 5. task_url 返回格式

**现状：** `task_url` **未返回**给小程序端。

**分析：**
1. `TaskItemDTO` 无 `taskUrl` 字段
2. `selectTaskItemsByPlanIdAndUserId` SQL 未查询 `task_url`
3. 前端无法拿到视频/资源链接

**如需返回 resourceUrl，有两种方式：**
1. **短期：** 修改 SQL，追加 `t.task_url AS resourceUrl`，同时在 `TaskItemDTO` 新增 `private String resourceUrl;`
2. **长期：** 新建 `TaskItemDetailDTO` 扩展字段，专门用于课程详情页

---

## 三句话总结

| 字段 | 现状 |
|------|------|
| **task_type** | DTO 和 SQL 均存在，但 **未做 UPPER() 强制大写**，依赖数据库存储时已大写 |
| **task_url** | **未返回**给小程序端，DTO 和 SQL 均无此字段，需前端配合确定是否需要 |
| **sort_order** | **存在**，SQL 中用于 `ORDER BY`，表结构正常 |