# 新增排课（追加天数）API 代码追溯报告

---

## 一、Controller 层

**文件**：`CampPlanController.java`（第 113–130 行）

```java
/**
 * 智能追加一天排课
 * POST /api/admin/camp-plans/add-smart-day
 *
 * 前端智能推算完整数据后，后端仅负责落库
 *
 * @param requestDTO 智能追加排课请求 DTO
 * @return 统一响应结果
 */
@PostMapping("/add-smart-day")
public ResponseResult<Void> addSmartDay(@RequestBody @Validated CampPlanAddDayDTO requestDTO) {
    campPlanService.addSmartDay(requestDTO);
    return ResponseResult.successMsg("智能追加排课成功");
}
```

**返回值**：返回 `ResponseResult<Void>`，即 **只返回一个成功字符串 "智能追加排课成功"，不包含任何数据对象**。

---

## 二、Service 层

**文件**：`CampPlanServiceImpl.java`（第 384–406 行）

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void addSmartDay(CampPlanAddDayDTO requestDTO) {
    // 1. 校验营期是否存在
    Camp camp = campMapper.selectById(requestDTO.getCampId());
    if (camp == null) {
        throw new BusinessException(400, "未找到指定的营期");
    }

    // 2. 将 DTO 属性拷贝到实体中
    CampPlan campPlan = new CampPlan();
    BeanUtils.copyProperties(requestDTO, campPlan);

    // 3. 强制设置默认未完成状态
    campPlan.setIsFinished(0);

    // 4. 铁桶防御：捕获唯一索引冲突异常
    try {
        campPlanMapper.insertCampPlan(campPlan);
    } catch (DuplicateKeyException e) {
        throw new BusinessException(409, "该营期的当前天数或日期已被占用，请刷新页面获取最新排课进度！");
    }
}
```

**关键点**：
- 方法返回类型为 `void`，不返回任何对象
- 通过 `BeanUtils.copyProperties(requestDTO, campPlan)` 将 DTO 属性拷贝到实体
- 调用 `campPlanMapper.insertCampPlan(campPlan)` 插入数据
- `insertCampPlan` 配置了 `useGeneratedKeys="true" keyProperty="planId"`，插入后 `campPlan.planId` 会被自动赋值（但 Service 层没有使用它）

---

## 三、Mapper XML

**文件**：`CampPlanMapper.xml`（第 50–57 行）

```xml
<insert id="insertCampPlan" parameterType="com.daily.dailychineseculture.entity.CampPlan"
        useGeneratedKeys="true" keyProperty="planId">
    INSERT INTO t_camp_plan
        (camp_id, day_index, plan_date, title, module_index, module_name, teacher_name)
    VALUES
        (#{campId}, #{dayIndex}, #{planDate}, #{title},
         #{moduleIndex}, #{moduleName}, #{teacherName})
</insert>
```

**主键回填配置**：✅ 已配置
- `useGeneratedKeys="true"`：开启主键回填
- `keyProperty="planId"`：将数据库自增主键回填到 Java 实体 `campPlan.planId` 属性

---

## 四、数据流总结

```
前端 POST /api/admin/camp-plans/add-smart-day
        │
        ▼
CampPlanController.addSmartDay(CampPlanAddDayDTO)
        │  ⚠️ 返回类型为 ResponseResult<Void>
        │  ⚠️ 只返回 successMsg，不返回任何 DTO
        ▼
CampPlanServiceImpl.addSmartDay(requestDTO)
        │
        ├─ BeanUtils.copyProperties(DTO → Entity)
        │    ✅ moduleIndex/moduleName/teacherName 均在 DTO 中定义
        │       会正确拷贝到 campPlan 实体
        │
        ├─ campPlan.setIsFinished(0)
        │
        └─ campPlanMapper.insertCampPlan(campPlan)
              ✅ INSERT SQL 已包含 module_index/module_name/teacher_name
              ✅ useGeneratedKeys + keyProperty="planId" 已配置
                   → 插入后 campPlan.planId 被自动赋值
                   → 但 Service 层 void，未将此值返回给 Controller
              ✅ Controller 层 void 返回，不向前端返回新增记录的 planId
```

---

## 五、接口现状总结

| 项目 | 现状 |
|------|------|
| **Controller 返回类型** | `ResponseResult<Void>` |
| **Controller 返回内容** | 仅 `"智能追加排课成功"` 字符串，无数据 |
| **Service 返回类型** | `void` |
| **主键回填** | ✅ 已配置（`useGeneratedKeys="true"`） |
| **planId 是否返回前端** | ❌ 否（Service void + Controller 返回 Void） |
| **字段完整性** | ✅ INSERT 已包含 module_index/module_name/teacher_name（本次修复后） |
