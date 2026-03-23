# 课程数据 API 实现逻辑总结

## 1. 接口基础信息

- **API 路径**：`GET /courses/{campId}/data`（小程序端，无 `/api/` 前缀）
- **HTTP Method**：`GET`
- **请求参数**：
  - `campId`：通过路径变量 `@PathVariable Integer campId` 接收
  - `userId`：前端不显式传参，后端通过 `request.getAttribute("userId")` 获取
    - 该值由 `AuthInterceptor` 解析 `Authorization` 中 JWT 后注入请求上下文

---

## 2. 数据库与 SQL 逻辑 (MyBatis)

### 2.1 涉及的数据库表

- `t_camp_plan`：课程排课计划（用于总天数与趋势基准）
- `t_user_daily_record`：用户每日学习记录（用于完成天数、完成率、趋势点位）

> 当前“课程数据”接口未直接查询 `t_camp_enrollment`。

### 2.2 关键 SQL

#### A. 查询营期全部排课（用于“总天数”）

```sql
SELECT 
    plan_id,
    camp_id,
    day_index,
    plan_date,
    title,
    video_url,
    graphic_url,
    module_index,
    module_name,
    reading_title,
    teacher_name,
    video_duration,
    extra_task1_name,
    extra_task2_name,
    extra_task3_name
FROM t_camp_plan
WHERE camp_id = #{campId}
ORDER BY day_index ASC
```

#### B. 查询用户在营期内全部学习记录

```sql
SELECT * 
FROM t_user_daily_record 
WHERE user_id = #{userId} 
  AND camp_id = #{campId}
```

#### C. 查询“已发生”的最近 7 节课（用于趋势）

```sql
SELECT 
    plan_id,
    camp_id,
    day_index,
    plan_date,
    title,
    video_url,
    graphic_url,
    module_index,
    module_name,
    reading_title,
    teacher_name,
    video_duration,
    extra_task1_name,
    extra_task2_name
FROM t_camp_plan
WHERE camp_id = #{campId}
  AND DATE(plan_date) <= DATE(#{today})
ORDER BY day_index DESC
LIMIT 7
```

### 2.3 指标计算规则

- **总天数 totalDays**：`t_camp_plan` 查询结果数量
- **已完成天数 completedDays**：`t_user_daily_record` 中 `completion_rate == 100` 的记录数
- **总完成率 overallRate**：
  - 分子：`completedDays * 100`
  - 分母：`totalDays`
  - 公式：`overallRate = (totalDays > 0) ? (completedDays * 100) / totalDays : 0`
  - 说明：整数运算，向下取整

---

## 3. 学习趋势数据聚合

- 返回字段：`trends`
- 单项结构：`TrendItemDTO { dayStr, rate }`
  - `dayStr`：如 `Day12`
  - `rate`：该节课对应的 `completionRate`（无记录则 0）

### 聚合过程

1. 查询 `plan_date <= today` 的最近 7 节课（按 `day_index DESC`）
2. 在 Java 内存中 `reverse` 反转为升序展示
3. 遍历每个 plan：
   - `dayStr = "Day" + dayIndex`
   - 用 `planId` 在 `recordList` 中匹配用户记录，取 `completionRate` 作为该点 `rate`
4. 组装为 `trends` 列表返回

> 该趋势是“最近 7 节已发生课程”的完成率，不是按自然日统计，也不是按模块汇总。

---

## 4. 返回 JSON 结构示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "totalDays": 30,
    "completedDays": 9,
    "overallRate": 30,
    "trends": [
      { "dayStr": "Day12", "rate": 100 },
      { "dayStr": "Day13", "rate": 80 },
      { "dayStr": "Day14", "rate": 100 },
      { "dayStr": "Day15", "rate": 60 },
      { "dayStr": "Day16", "rate": 100 },
      { "dayStr": "Day17", "rate": 0 },
      { "dayStr": "Day18", "rate": 40 }
    ],
    "achievements": [
      {
        "icon": "https://img.icons8.com/color/96/medal2.png",
        "title": "初学者",
        "desc": "完成第一天学习，开启致良知之旅"
      },
      {
        "icon": "https://img.icons8.com/color/96/warranty.png",
        "title": "渐入佳境",
        "desc": "累计完成三天学习"
      }
    ]
  }
}
```
