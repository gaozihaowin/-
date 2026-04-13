# 新增排课计划字段丢失 Bug 分析报告

## 一、接口入口

**Controller**：`CampPlanController.java`

```java
@RestController
@RequestMapping("/api/admin/camp-plans")
@RequiredArgsConstructor
public class CampPlanController {

    private final CampPlanService campPlanService;

    /**
     * 新增一天的排课
     * POST /api/admin/camp-plans
     *
     * @param campPlan 排课计划 DTO（包含 campId, dayIndex, planDate 等基本信息）
     * @return 统一响应结果，包含新增后的排课计划（含 planId）
     */
    @PostMapping
    public ResponseResult<CampPlanDTO> addCampPlan(@RequestBody CampPlanDTO campPlan) {
        CampPlanDTO result = campPlanService.addCampPlan(campPlan);
        return ResponseResult.success("新增成功", result);
    }

    /**
     * 保存/更新单日课表
     * PUT /api/admin/camp-plans/save-day
     */
    @PutMapping("/save-day")
    public ResponseResult<String> saveDay(@Valid @RequestBody CampPlanSaveDayDTO request) {
        campPlanService.saveDayPlan(request);
        return ResponseResult.success("保存成功");
    }
}
```

---

## 二、Service 层

**接口**：`CampPlanService.java`（第 28 行）

```java
CampPlanDTO addCampPlan(CampPlanDTO campPlan);
void saveDayPlan(CampPlanSaveDayDTO request);
```

**实现**：`CampPlanServiceImpl.java`（第 200–225 行）

```java
@Override
@Transactional(rollbackFor = Exception.class)
public CampPlanDTO addCampPlan(CampPlanDTO campPlan) {
    // 1. 校验营期是否存在
    Camp camp = campMapper.selectById(campPlan.getCampId());
    if (camp == null) {
        throw new RuntimeException("未找到指定的营期");
    }

    // 2. 创建排课计划实体
    CampPlan plan = new CampPlan();
    plan.setCampId(campPlan.getCampId());
    plan.setDayIndex(campPlan.getDayIndex());
    plan.setPlanDate(campPlan.getPlanDate());
    plan.setTitle(campPlan.getTitle() != null ? campPlan.getTitle() : "");

    // ⚠️ 注意：以下三个字段虽然在此处设置，但 Insert SQL 中并没有这些列
    plan.setModuleIndex(campPlan.getModuleIndex());  // 设置了，但 SQL 漏了
    plan.setModuleName(campPlan.getModuleName());    // 设置了，但 SQL 漏了
    plan.setTeacherName(campPlan.getTeacherName());  // 设置了，但 SQL 漏了

    // 3. 插入排课计划
    campPlanMapper.insertCampPlan(plan);

    // 4. 如果有任务列表，同步插入任务
    if (campPlan.getTasks() != null && !campPlan.getTasks().isEmpty()) {
        for (PlanTaskDTO taskDTO : campPlan.getTasks()) {
            PlanTask newTask = convertToEntity(plan.getPlanId(), taskDTO);
            planTaskMapper.insertTask(newTask);
        }
        List<PlanTaskDTO> tasks = planTaskMapper.selectTasksByPlanId(plan.getPlanId());
        campPlan.setTasks(tasks);
    }

    // 5. 设置返回的 planId
    campPlan.setPlanId(plan.getPlanId());

    return campPlan;
}
```

**saveDayPlan 实现**：`CampPlanServiceImpl.java`（第 268–320 行）

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void saveDayPlan(CampPlanSaveDayDTO request) {
    Integer planId = request.getId();

    // 1. 更新主表（⚠️ updateCampPlan SQL 只更新 title，以下字段全部被忽略）
    CampPlanDTO planDTO = new CampPlanDTO();
    planDTO.setPlanId(planId);
    planDTO.setCampId(request.getCampId());
    planDTO.setTitle(request.getTitle());
    // ⚠️ moduleIndex、moduleName、teacherName 根本没有在 planDTO 中设置
    // ⚠️ 而且 updateCampPlan SQL 也只更新 title
    campPlanMapper.updateCampPlan(planDTO);

    // 2. 准备数据
    List<CampPlanSaveDayDTO.CampTask> incoming = request.getTasks();
    if (incoming == null) incoming = new ArrayList<>();
    List<Integer> existingIds = planTaskMapper.selectTaskIdsByPlanId(planId);

    // 3. 分拣：有 taskId → 更新，无 taskId → 新增
    List<PlanTask> toUpdate = new ArrayList<>();
    List<PlanTask> toInsert = new ArrayList<>();
    Set<Integer> incomingIds = new HashSet<>();

    for (int i = 0; i < incoming.size(); i++) {
        CampPlanSaveDayDTO.CampTask dto = incoming.get(i);
        PlanTask task = new PlanTask();
        task.setPlanId(planId);
        task.setTaskName(dto.getTaskName());
        task.setTaskType(dto.getTaskType().toUpperCase());
        task.setTaskDesc(dto.getTaskDesc());
        task.setTaskUrl(dto.getTaskUrl());
        task.setDuration(dto.getDuration());
        task.setIsRequired(dto.getIsRequired());
        task.setSortOrder(i + 1);

        if (dto.getTaskId() != null) {
            task.setTaskId(dto.getTaskId());
            incomingIds.add(dto.getTaskId());
            toUpdate.add(task);
        } else {
            toInsert.add(task);
        }
    }

    // 4. 找出要删除的 ID
    List<Integer> toDeleteIds = existingIds.stream()
            .filter(id -> !incomingIds.contains(id))
            .collect(Collectors.toList());

    // 5. 先删 → 再改 → 再增
    if (!toDeleteIds.isEmpty()) planTaskMapper.deleteTasksByIds(toDeleteIds);
    if (!toUpdate.isEmpty())    planTaskMapper.batchUpdateTasks(toUpdate);
    if (!toInsert.isEmpty())    planTaskMapper.batchInsertTasks(toInsert);
}
```

---

## 三、DTO 定义

### CampPlanDTO.java（addCampPlan 使用）

```java
@Data
public class CampPlanDTO {
    private Integer planId;       // 计划 ID
    private Integer campId;       // 营期 ID
    private Integer dayIndex;     // 第几天
    private Date planDate;        // 具体日期
    private String title;         // 导读标题
    private String moduleName;    // 模块名称（✅ 字段存在）
    private Integer moduleIndex;  // 模块索引（✅ 字段存在）
    private String teacherName;   // 讲师姓名（✅ 字段存在）
    private List<PlanTaskDTO> tasks;
}
```

### CampPlanSaveDayDTO.java（saveDayPlan 使用）

```java
@Data
@Schema(description = "单日排课聚合保存请求")
public class CampPlanSaveDayDTO {

    @NotNull(message = "计划ID不能为空")
    private Integer id;

    @NotNull(message = "营期ID不能为空")
    private Integer campId;

    @Schema(description = "第几天", example = "1")
    private Integer dayNum;  // ⚠️ 注意：字段名是 dayNum，不是 dayIndex

    @NotBlank(message = "标题不能为空")
    private String title;

    // ⚠️ 缺失字段：moduleIndex（模块索引）
    // ⚠️ 缺失字段：moduleName（模块名称）
    // ⚠️ 缺失字段：teacherName（讲师姓名）

    @Schema(description = "任务列表")
    private List<CampTask> tasks;

    @Data
    public static class CampTask {
        private Integer taskId;
        @NotBlank
        private String taskType;
        @NotBlank
        private String taskName;
        private String taskDesc;
        private String taskUrl;
        private Integer duration;
        private Integer isRequired;
        private Integer sortOrder;
    }
}
```

---

## 四、Mapper XML

### insertCampPlan（Bug 所在）

```xml
<!-- 插入单条排课计划 -->
<insert id="insertCampPlan" parameterType="com.daily.dailychineseculture.entity.CampPlan" useGeneratedKeys="true" keyProperty="planId">
    INSERT INTO t_camp_plan (camp_id, day_index, plan_date, title)
    VALUES (#{campId}, #{dayIndex}, #{planDate}, #{title})
    <!-- ⚠️ 缺失：module_index, module_name, teacher_name -->
</insert>
```

### updateCampPlan（Bug 所在）

```xml
<!-- 根据 ID 更新排课计划 -->
<update id="updateCampPlan" parameterType="com.daily.dailychineseculture.dto.CampPlanDTO">
    UPDATE t_camp_plan
    <set>
        <if test="title != null">
            title = #{title},
        </if>
        <!-- ⚠️ 缺失：module_index, module_name, teacher_name, day_index, plan_date, camp_id -->
    </set>
    WHERE plan_id = #{planId}
</update>
```

---

## 五、Bug 根因总结

### Bug 1：POST /api/admin/camp-plans 新增时字段丢失

| 字段 | Service 设置了？ | SQL 插入了？ | 结果 |
|---|---|---|---|
| `camp_id` | ✅ | ✅ | 正常 |
| `day_index` | ✅ | ✅ | 正常 |
| `plan_date` | ✅ | ✅ | 正常 |
| `title` | ✅ | ✅ | 正常 |
| `module_index` | ✅ | ❌ 缺失 | **0（默认值）** |
| `module_name` | ✅ | ❌ 缺失 | **NULL** |
| `teacher_name` | ✅ | ❌ 缺失 | **NULL** |

**根因**：`CampPlanMapper.xml` 的 `insertCampPlan` SQL 只包含 4 个字段，`module_index`、`module_name`、`teacher_name` 虽然在 Service 层被 set 到了实体中，但 INSERT SQL 中根本没有这些列，数据被数据库默认值（0/NULL）填充。

### Bug 2：PUT /api/admin/camp-plans/save-day 更新时字段丢失

| 字段 | DTO 有此字段？ | updateCampPlan SQL 更新了？ | 结果 |
|---|---|---|---|
| `title` | ✅ | ✅ | 正常 |
| `module_index` | ❌ DTO 缺失 | ❌ SQL 缺失 | **无法传入 & 无法更新** |
| `module_name` | ❌ DTO 缺失 | ❌ SQL 缺失 | **无法传入 & 无法更新** |
| `teacher_name` | ❌ DTO 缺失 | ❌ SQL 缺失 | **无法传入 & 无法更新** |

**根因**：三重缺失——DTO 缺少字段 → Service 无法传递 → SQL 无法写入。

---

## 六、修复方案

### 修复一：insertCampPlan SQL（新增接口）

```xml
<insert id="insertCampPlan" parameterType="com.daily.dailychineseculture.entity.CampPlan" useGeneratedKeys="true" keyProperty="planId">
    INSERT INTO t_camp_plan
        (camp_id, day_index, plan_date, title, module_index, module_name, teacher_name)
    VALUES
        (#{campId}, #{dayIndex}, #{planDate}, #{title}, #{moduleIndex}, #{moduleName}, #{teacherName})
</insert>
```

### 修复二：updateCampPlan SQL（更新接口）

```xml
<update id="updateCampPlan" parameterType="com.daily.dailychineseculture.dto.CampPlanDTO">
    UPDATE t_camp_plan
    <set>
        <if test="campId != null">camp_id = #{campId},</if>
        <if test="dayIndex != null">day_index = #{dayIndex},</if>
        <if test="planDate != null">plan_date = #{planDate},</if>
        <if test="title != null">title = #{title},</if>
        <if test="moduleIndex != null">module_index = #{moduleIndex},</if>
        <if test="moduleName != null">module_name = #{moduleName},</if>
        <if test="teacherName != null">teacher_name = #{teacherName},</if>
    </set>
    WHERE plan_id = #{planId}
</update>
```

### 修复三：CampPlanSaveDayDTO 补全缺失字段

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
    private Integer moduleIndex;  // ← 新增

    @NotBlank(message = "模块名称不能为空")
    @Schema(description = "模块名称", example = "知行合一")
    private String moduleName;     // ← 新增

    @Schema(description = "讲师姓名", example = "王老师")
    private String teacherName;    // ← 新增

    @NotBlank(message = "标题不能为空")
    @Schema(description = "单日标题", example = "第1天：心学导论与立志")
    private String title;

    @Schema(description = "任务列表")
    private List<CampTask> tasks;
    // ...
}
```

### 修复四：saveDayPlan 方法补全字段传递

```java
public void saveDayPlan(CampPlanSaveDayDTO request) {
    Integer planId = request.getId();

    // 1. 更新主表（补全所有字段）
    CampPlanDTO planDTO = new CampPlanDTO();
    planDTO.setPlanId(planId);
    planDTO.setCampId(request.getCampId());
    planDTO.setTitle(request.getTitle());
    planDTO.setModuleIndex(request.getModuleIndex());  // ← 新增
    planDTO.setModuleName(request.getModuleName());    // ← 新增
    planDTO.setTeacherName(request.getTeacherName());  // ← 新增
    campPlanMapper.updateCampPlan(planDTO);
    // ...
}
```
