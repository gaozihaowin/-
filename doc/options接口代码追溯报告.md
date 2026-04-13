# `options` 接口代码追溯报告

> **功能模块**：教务排课工作台
> **目标接口**：`GET /api/admin/camps/options`
> **用途**：为下拉框或级联选择器提供营期选项数据

---

## 一、Controller 层

**文件**：`src/main/java/com/daily/dailychineseculture/controller/CampController.java`

```java
@GetMapping("/options")
public ResponseResult<List<CampOptionDTO>> getCampOptions() {
    List<CampOptionDTO> options = campPlanService.getCampOptions();
    return ResponseResult.success("查询成功", options);
}
```

- **路由**：`GET /api/admin/camps/options`（类上标注 `@RequestMapping({"/api/admin/camps", "/camp"})`）
- **返回类型**：`ResponseResult<List<CampOptionDTO>>`

---

## 二、Service 层逻辑

**文件**：`src/main/java/com/daily/dailychineseculture/service/impl/CampPlanServiceImpl.java`（第 45-47 行）

```java
@Override
public List<CampOptionDTO> getCampOptions() {
    return campMapper.selectCampOptions();
}
```

**业务分析**：

- Service 层**无任何业务过滤逻辑**，直接透传 Mapper 查询结果。
- 未按 `status` 过滤，未按营期状态过滤，**返回全量营期数据**。
- 仅依赖 SQL 层按 `start_time DESC` 排序。

---

## 三、数据访问与映射 (Mapper/DAO & 实体)

### 3.1 SQL 查询逻辑

**文件**：`src/main/resources/mapper/CampMapper.xml`（第 93-100 行）

```xml
<select id="selectCampOptions" resultType="com.daily.dailychineseculture.dto.CampOptionDTO">
    SELECT
        camp_id AS campId,
        name,
        term
    FROM t_camp
    ORDER BY start_time DESC
</select>
```

| 项目 | 内容 |
|------|------|
| 查询表 | `t_camp` |
| 字段 | `camp_id`、`name`、`term` |
| 排序 | `ORDER BY start_time DESC`（按开营时间倒序） |
| WHERE 条件 | **无** — 返回全表所有营期 |

> **注意**：`t_camp` 表存在 `status` 字段（0=未开始, 1=进行中, 2=已结束），但当前 SQL 未对其做过滤。

### 3.2 Mapper 接口

**文件**：`src/main/java/com/daily/dailychineseculture/mapper/CampMapper.java`

```java
List<CampOptionDTO> selectCampOptions();
```

### 3.3 数据模型 DTO

**文件**：`src/main/java/com/daily/dailychineseculture/dto/CampOptionDTO.java`

```java
@Data
public class CampOptionDTO {
    private Integer campId;   // 营期 ID
    private String name;       // 营期名称（如"【致良知线上课堂】良知班"）
    private Integer term;     // 期数
}
```

---

## 四、完整数据流图

```
前端请求 GET /api/admin/camps/options
        │
        ▼
┌───────────────────────────────────────┐
│   CampController.getCampOptions        │  @GetMapping("/options")
└─────────────────┬─────────────────────┘
                  │ 调用
                  ▼
┌───────────────────────────────────────┐
│  CampPlanServiceImpl.getCampOptions   │  直接透传，无业务过滤
└─────────────────┬─────────────────────┘
                  │ 调用
                  ▼
┌───────────────────────────────────────┐
│     CampMapper.selectCampOptions       │
└─────────────────┬─────────────────────┘
                  │ SQL 执行
                  ▼
┌───────────────────────────────────────┐
│             t_camp (全表扫描)           │
│   SELECT camp_id, name, term           │
│   FROM t_camp                          │
│   ORDER BY start_time DESC             │
│   ⚠️ 无 WHERE status = ? 过滤          │
└─────────────────┬─────────────────────┘
                  │ 返回 List<CampOptionDTO>
                  ▼
┌───────────────────────────────────────┐
│       ResponseResult.success()         │
│   { code: 200, msg: "查询成功",         │
│     data: [ {campId, name, term}, ...] }│
└───────────────────────────────────────┘
```

---

## 五、当前问题与改进建议

| 问题 | 说明 | 建议 |
|------|------|------|
| **无 status 过滤** | `t_camp` 表有 `status` 字段（0=未开始, 1=进行中, 2=已结束），但 SQL 未过滤，下拉框会混入已结业营期 | 在 SQL 中增加 `WHERE status IN (0, 1)` 或 `WHERE status = 1` 过滤进行中/未开始的营期 |
| **无分页** | 数据量大时全量返回可能有性能问题 | 考虑增加分页或上限 `LIMIT` |
| **无权限校验** | 接口未校验当前登录用户身份，任何人可调用 | 建议增加 `SUPER_ADMIN` 或 `COURSE_ADMIN` 角色校验 |
