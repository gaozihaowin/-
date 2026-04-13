# 排课计划保存接口 /save-day — 完整代码链路梳理

> 整理目的：便于前后端联调 Bug 排查，对齐接口契约与数据库操作。
> 整理时间：2026-04-10

---

## 1. Controller 层

**文件：** `CampPlanController.java`

```java
@PutMapping("/save-day")
public ResponseResult<String> saveDay(@Valid @RequestBody CampPlanSaveDayDTO request) {
    campPlanService.saveDayPlan(request);
    return ResponseResult.success("保存成功");
}
```

| 项目 | 值 |
|------|-----|
| 接口路径 | `PUT /api/admin/camp-plans/save-day` |
| 请求方式 | HTTP PUT |
| Content-Type | `application/json` |
| 注解 | `@Valid` 开启参数校验，`@RequestBody` 接收 JSON |
| 返回格式 | `ResponseResult<String>`（仅返回成功文案） |

---

## 2. 请求参数 DTO 结构

**文件：** `CampPlanSaveDayDTO.java`

```java
@Data
@Schema(description = "单日排课聚合保存请求")
public class CampPlanSaveDayDTO {

    @NotNull(message = "计划ID不能为空")
    @Schema(description = "计划ID", example = "1")
    private Integer id;

    @NotNull(message = "营期ID不能为空")
    @Schema(description = "营期ID", example = "101")
    private Integer campId;

    @NotNull(message = "模块索引不能为空")
    @Schema(description = "模块索引（第几周）", example = "1")
    private Integer moduleIndex;

    @NotBlank(message = "模块名称不能为空")
    @Schema(description = "模块名称", example = "知行合一")
    private String moduleName;

    @Schema(description = "讲师姓名", example = "王阳明")
    private String teacherName;

    @Schema(description = "第几天", example = "1")
    private Integer dayNum;

    @NotBlank(message = "标题不能为空")
    @Schema(description = "单日标题", example = "第1天：心学导论与立志")
    private String title;

    @Schema(description = "任务列表")
    private List<CampTask> tasks;

    @Data
    @Schema(description = "任务信息")
    public static class CampTask {

        @Schema(description = "任务ID（新增时为null）", example = "10")
        private Integer taskId;

        @NotBlank(message = "任务类型不能为空")
        @Schema(description = "任务类型", allowableValues = {"READ", "VIDEO", "HOMEWORK", "EXTRA"})
        private String taskType;

        @NotBlank(message = "任务名称不能为空")
        @Schema(description = "任务名称", example = "原典精读：经典篇目")
        private String taskName;

        @Schema(description = "任务说明", example = "请认真阅读以下经典篇目")
        private String taskDesc;

        @Schema(description = "资源链接", example = "http://localhost:8080/uploads/videos/xxx.mp4")
        private String taskUrl;

        @Schema(description = "关联课件ID（来自课件中台）", example = "1")
        private Long materialId;

        @Schema(description = "建议时长（分钟）", example = "30")
        private Integer duration;

        @Schema(description = "是否必修（1必修，0选修）", example = "1")
        private Integer isRequired;

        @Schema(description = "排序序号", example = "1")
        private Integer sortOrder;
    }
}
```

### ⚠️ 重要字段说明

| 字段 | 说明 |
|------|------|
| `id`（顶层） | 对应 `t_camp_plan.plan_id`，必填 |
| `campId` | 对应 `t_camp.camp_id`，必填 |
| `tasks[].taskId` | `null` = 新增，非 null = 更新 |
| `tasks[].materialId` | 课件中台 ID，非空则触发中台 URL 同步逻辑 |
| `tasks[].taskUrl` | 如果 `materialId` 非空，此字段会被**中台 URL 强制覆盖** |

---

## 3. Service 层核心业务逻辑

**文件：** `CampPlanServiceImpl.java` → `saveDayPlan()` 方法

```java
/**
 * 聚合保存单日排课（主表+任务列表全量刷新）
 * 全删全插策略：
 * 1. 更新 CampPlan 主表信息
 * 2. 物理删除该日所有旧任务
 * 3. 遍历前端任务列表，强制设置 planId 并置空 ID，批量插入新任务
 */
@Override
@Transactional(rollbackFor = Exception.class)
public void saveDayPlan(CampPlanSaveDayDTO request) {
    Integer planId = request.getId();

    // ========== 1. 更新主表 t_camp_plan ==========
    CampPlanDTO planDTO = new CampPlanDTO();
    planDTO.setPlanId(planId);
    planDTO.setCampId(request.getCampId());
    planDTO.setTitle(request.getTitle());
    planDTO.setModuleIndex(request.getModuleIndex());
    planDTO.setModuleName(request.getModuleName());
    planDTO.setTeacherName(request.getTeacherName());
    campPlanMapper.updateCampPlan(planDTO);

    // ========== 2. 准备数据 ==========
    List<CampPlanSaveDayDTO.CampTask> incoming = request.getTasks();
    if (incoming == null) incoming = new ArrayList<>();
    List<Integer> existingIds = planTaskMapper.selectTaskIdsByPlanId(planId);

    // ========== 3. 分拣：有 taskId → 更新，无 taskId → 新增 ==========
    List<PlanTask> toUpdate = new ArrayList<>();
    List<PlanTask> toInsert = new ArrayList<>();
    Set<Integer> incomingIds = new HashSet<>();

    for (int i = 0; i < incoming.size(); i++) {
        CampPlanSaveDayDTO.CampTask dto = incoming.get(i);

        // 【关键】中台强制同步逻辑
        PlanTask task = convertToEntity(planId, dto);

        if (dto.getTaskId() != null) {
            task.setTaskId(dto.getTaskId());
            incomingIds.add(dto.getTaskId());
            toUpdate.add(task);
        } else {
            toInsert.add(task);
        }
    }

    // ========== 4. 找出要删除的 ID（数据库有，前端没有） ==========
    List<Integer> toDeleteIds = existingIds.stream()
            .filter(id -> !incomingIds.contains(id))
            .collect(Collectors.toList());

    // ========== 5. 先删 → 再改 → 再增 ==========
    if (!toDeleteIds.isEmpty()) planTaskMapper.deleteTasksByIds(toDeleteIds);
    if (!toUpdate.isEmpty())    planTaskMapper.batchUpdateTasks(toUpdate);
    if (!toInsert.isEmpty())    planTaskMapper.batchInsertTasks(toInsert);
}

/**
 * 将 DTO 转换为实体（带课件中台强制同步逻辑）
 */
private PlanTask convertToEntity(Integer planId, CampPlanSaveDayDTO.CampTask dto) {
    PlanTask task = new PlanTask();
    task.setPlanId(planId);
    task.setTaskName(dto.getTaskName());
    task.setTaskType(dto.getTaskType().toUpperCase());
    task.setTaskDesc(dto.getTaskDesc());
    task.setIsRequired(dto.getIsRequired());
    task.setSortOrder(dto.getSortOrder());

    if (dto.getMaterialId() != null) {
        CourseMaterial material = courseMaterialMapper.selectById(dto.getMaterialId());
        if (material != null) {
            task.setMaterialId(dto.getMaterialId());
            task.setTaskUrl(material.getUrl());      // 强制使用中台 URL
            if (dto.getDuration() == null || dto.getDuration() == 0) {
                task.setDuration(material.getDuration());  // 中台时长兜底
            } else {
                task.setDuration(dto.getDuration());
            }
        } else {
            throw new BusinessException("所选的课件资源不存在或已被删除，请刷新后重试！");
        }
    } else {
        task.setMaterialId(null);
        task.setTaskUrl(dto.getTaskUrl());
        task.setDuration(dto.getDuration());
    }
    return task;
}
```

### 业务逻辑流程图

```
前端提交 tasks[]
    │
    ▼
遍历每个 CampTask
    │
    ├── taskId == null ──→ 新增 → toInsert[]
    │
    └── taskId != null ──→ 更新 → toUpdate[]
    │
    ▼
所有前端 taskId → incomingIds{}
    │
    ▼
existingIds - incomingIds = toDeleteIds[]  （数据库有，前端没有 → 删）
    │
    ▼
执行顺序：先删 → 再改 → 再增
```

### 双轨制 URL 同步逻辑

| 场景 | taskUrl 来源 | duration 来源 |
|------|-------------|--------------|
| `materialId != null` + 课件存在 | 中台 `material.url`（强制覆盖） | 中台 `material.duration`（仅当 DTO 为空/0 时） |
| `materialId != null` + 课件不存在 | **抛出 BusinessException** | — |
| `materialId == null` | DTO `taskUrl`（直传） | DTO `duration`（直传） |

---

## 4. Mapper 接口与 XML SQL

### 4.1 CampPlanMapper — 更新主表

**接口：** `campPlanMapper.updateCampPlan(CampPlanDTO)`

**XML：**
```xml
<!-- CampPlanMapper.xml -->
<update id="updateCampPlan" parameterType="com.daily.dailychineseculture.dto.CampPlanDTO">
    UPDATE t_camp_plan
    <set>
        <if test="campId != null">camp_id = #{campId},</if>
        <if test="dayIndex != null">day_index = #{dayIndex},</if>
        <if test="planDate != null">plan_date = #{planDate},</if>
        <if test="title != null and title != ''">title = #{title},</if>
        <if test="moduleIndex != null">module_index = #{moduleIndex},</if>
        <if test="moduleName != null and moduleName != ''">module_name = #{moduleName},</if>
        <if test="teacherName != null and teacherName != ''">teacher_name = #{teacherName},</if>
    </set>
    WHERE plan_id = #{planId}
</update>
```

### 4.2 PlanTaskMapper — 任务相关操作

**三个核心方法：**

#### (a) 查询现有任务 ID 列表
```xml
<!-- PlanTaskMapper.xml -->
<select id="selectTaskIdsByPlanId" resultType="int">
    SELECT task_id
    FROM t_plan_task
    WHERE plan_id = #{planId}
      AND is_deleted = 0
</select>
```

#### (b) 批量插入任务
```xml
<insert id="batchInsertTasks" parameterType="java.util.List">
    INSERT INTO t_plan_task (
        plan_id, task_type, task_name, task_desc,
        task_url, material_id, duration,
        is_required, sort_order, is_deleted
    ) VALUES
    <foreach collection="tasks" item="task" separator=",">
        (
            #{task.planId},
            #{task.taskType},
            #{task.taskName},
            #{task.taskDesc},
            #{task.taskUrl},
            #{task.materialId},
            #{task.duration},
            #{task.isRequired},
            #{task.sortOrder},
            0
        )
    </foreach>
</insert>
```

> ⚠️ **注意**：`batchInsertTasks` SQL 中 **没有 `material_id`**！这意味着批量插入的任务不会写入 `materialId`，只有单个插入 `insertTask` 才包含 `material_id`。

#### (c) 批量删除任务
```xml
<delete id="deleteTasksByIds">
    DELETE FROM t_plan_task WHERE task_id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</delete>
```

#### (d) 批量更新任务
```xml
<update id="batchUpdateTasks">
    <foreach collection="tasks" item="task" separator=";">
        UPDATE t_plan_task
        SET task_name       = #{task.taskName},
            task_type       = #{task.taskType},
            task_desc       = #{task.taskDesc},
            task_url        = #{task.taskUrl},
            duration        = #{task.duration},
            is_required     = #{task.isRequired},
            sort_order      = #{task.sortOrder}
        WHERE task_id = #{task.taskId}
    </foreach>
</update>
```

> ⚠️ **注意**：`batchUpdateTasks` SQL 中也 **没有 `material_id`**！

---

## ⚠️ 已知 Bug 与联调注意事项

### Bug 1：`batchInsertTasks` 和 `batchUpdateTasks` 缺失 `material_id`

**问题描述：**
- `batchInsertTasks` SQL 缺少 `material_id` 字段，导致批量新增的任务无法写入 `materialId`
- `batchUpdateTasks` SQL 同样缺少 `material_id` 和 `materialId` 相关更新

**影响场景：**
- 前端通过课件中台选择资源（`materialId != null`）时：
  - 新增任务：URL 同步正常（`convertToEntity` 中设置了 `taskUrl`），但 `material_id` 列本身不会被写入
  - 更新任务：同样 `material_id` 不会被更新

**数据库表现：**
- `t_plan_task.material_id` 列值可能为 `null`（即使当时是通过中台选择的资源）

**修复建议：**
在 `batchInsertTasks` 和 `batchUpdateTasks` SQL 中补全 `material_id` 相关字段。

---

## 数据库表字段对照

### t_camp_plan（排课主表）

| 字段 | 类型 | 说明 |
|------|------|------|
| plan_id | int | 主键 |
| camp_id | int | 营期 ID |
| day_index | int | 天索引 |
| plan_date | date | 计划日期 |
| title | varchar | 单日标题 |
| module_index | int | 模块索引 |
| module_name | varchar | 模块名称 |
| teacher_name | varchar | 讲师姓名 |

### t_plan_task（任务表）

| 字段 | 类型 | 说明 |
|------|------|------|
| task_id | int | 主键 |
| plan_id | int | 排课计划 ID |
| task_type | varchar | 任务类型 READ/VIDEO/HOMEWORK/EXTRA |
| task_name | varchar | 任务名称 |
| task_desc | varchar | 任务说明 |
| task_url | varchar | 资源链接 |
| material_id | bigint | 关联课件中台 ID（可为空） |
| duration | int | 时长（分钟） |
| is_required | int | 是否必修 |
| sort_order | int | 排序序号 |
| is_deleted | int | 逻辑删除标记 |

---

## 接口完整调用示例

### 请求
```json
PUT /api/admin/camp-plans/save-day
Content-Type: application/json

{
  "id": 10,
  "campId": 101,
  "moduleIndex": 1,
  "moduleName": "知行合一",
  "teacherName": "王阳明",
  "dayNum": 1,
  "title": "第1天：心学导论与立志",
  "tasks": [
    {
      "taskId": null,
      "taskType": "VIDEO",
      "taskName": "阳明心学概论",
      "taskDesc": "观看视频课程",
      "materialId": 5,
      "taskUrl": "http://fake-url.com/xxx.mp4",
      "duration": 30,
      "isRequired": 1,
      "sortOrder": 1
    },
    {
      "taskId": 42,
      "taskType": "READ",
      "taskName": "原典精读",
      "taskDesc": "阅读《传习录》",
      "materialId": null,
      "taskUrl": "http://localhost:8080/uploads/videos/yyy.mp4",
      "duration": 45,
      "isRequired": 1,
      "sortOrder": 2
    }
  ]
}
```

### 响应
```json
{
  "code": 200,
  "msg": "保存成功",
  "data": null,
  "message": "保存成功",
  "timestamp": 1744260836000
}
```

### 预期数据库变化（taskId=42 为更新，其余为新增）

| 操作 | taskId | 说明 |
|------|--------|------|
| 新增 | 分配新 ID | `task_url` = 中台 URL（`material_id=5` 对应的 url） |
| 更新 | 42 | `task_url` = 前端传入值（`materialId == null`，直传） |
