# 营期详情接口 endTime 字段检查报告

---

## 一、定位营期详情接口

### 1.1 Controller 层

**文件路径**：`src/main/java/com/daily/dailychineseculture/controller/CourseController.java`

```java
@GetMapping("/detail")
public Result<Course> getCourseDetail(@RequestParam Integer id) {
    try {
        if (id == null) {
            return Result.error("课程ID不能为空");
        }
        Course course = courseService.getCourseDetail(id);
        return Result.success(course);
    } catch (Exception e) {
        return Result.error("获取课程详情失败：" + e.getMessage());
    }
}
```

**接口路径**：`GET /courses/detail?id=xxx`（小程序端无 api/ 前缀）

---

### 1.2 Service 层

**文件路径**：`src/main/java/com/daily/dailychineseculture/service/impl/CourseServiceImpl.java`

```java
@Override
public Course getCourseDetail(Integer id) {
    Course course = courseMapper.selectCourseById(id);
    if (course == null) {
        throw new IllegalArgumentException("营期不存在，campId: " + id);
    }
    return course;
}
```

---

### 1.3 Mapper 层

**文件路径**：`src/main/java/com/daily/dailychineseculture/mapper/CourseMapper.java`

```java
@Select("SELECT " +
        "camp_id AS id, " +
        "name AS title, " +
        "name AS campName, " +
        "CONCAT('第', term, '期') AS batch, " +
        "intro AS description, " +
        "enroll_count AS participantCount, " +
        "status, " +
        "start_time AS startTime, " +
        "end_time AS endTime " +
        "FROM t_camp " +
        "WHERE camp_id = #{id}")
Course selectCourseById(@Param("id") Integer id);
```

---

## 二、实体类检查

### 2.1 Course.java（课程实体类）

**文件路径**：`src/main/java/com/daily/dailychineseculture/entity/Course.java`

```java
package com.daily.dailychineseculture.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;
import java.util.Date;

@Data
@Alias("Course")
public class Course {
    private Integer id;                 // 课程ID (对应 camp_id)
    private String title;               // 课程标题 (对应 name)
    private String campName;            // 营期名称 (对应 name)
    private String batch;               // 批次/期数 (CONCAT('第', term, '期'))
    private String description;         // 课程描述 (对应 intro)
    private Integer participantCount;   // 参与人数 (对应 enroll_count)
    private Integer status;             // 状态：1=招生中/开课中，0=已结束，-1=下架
    private Date startTime;             // 开始时间 (对应 start_time)
    private Date endTime;               // 结束时间 (对应 end_time) ✅ 已包含
}
```

**结论**：`Course` 实体类中**已包含 `endTime` 字段**。

---

## 三、SQL 查询分析

### 当前 SQL（selectCourseById）

```sql
SELECT
    camp_id AS id,
    name AS title,
    name AS campName,
    CONCAT('第', term, '期') AS batch,
    intro AS description,
    enroll_count AS participantCount,
    status,                      -- ⚠️ 物理字段，未做 CASE WHEN 计算
    start_time AS startTime,
    end_time AS endTime          -- ✅ 已查询
FROM t_camp
WHERE camp_id = #{id}
```

**字段映射确认**：
| 数据库字段 | 实体属性 | 状态 |
|-----------|---------|------|
| `camp_id` | `id` | ✅ |
| `name` | `title`, `campName` | ✅ |
| `term` | `batch` | ✅ |
| `intro` | `description` | ✅ |
| `enroll_count` | `participantCount` | ✅ |
| `status` | `status` | ⚠️ 物理字段 |
| `start_time` | `startTime` | ✅ |
| `end_time` | `endTime` | ✅ **已包含** |

---

## 四、问题分析

### 当前状态

1. **`endTime` 字段已存在于 `Course` 实体中**
2. **SQL 中已查询 `end_time AS endTime`**
3. **接口应返回完整的 `Course` 对象，包含 `endTime`**

### ⚠️ 潜在问题

当前 `selectCourseById` SQL 返回的是**物理 `status` 字段**，而不是根据时间计算的动态 status。这与营期列表接口（`selectActiveCourses`）的行为不一致。

**营期列表接口 `selectActiveCourses` 的 SQL**：
```java
@Select("SELECT " +
        "camp_id AS id, " +
        ...
        "CASE WHEN NOW() < start_time THEN 0 " +
        "WHEN NOW() > end_time THEN 2 " +
        "ELSE 1 END AS status, " +   -- ✅ 使用 CASE WHEN 动态计算
        "start_time AS startTime, " +
        "end_time AS endTime " +
        "FROM t_camp " +
        "WHERE status = 1 " +        -- 先用物理 status 过滤
        "AND end_time >= NOW() " +   -- 再用时间过滤
        ...
```

**详情接口 `selectCourseById` 的 SQL**：
```java
"status, " +                        -- ⚠️ 使用物理字段
"start_time AS startTime, " +
"end_time AS endTime " +
```

---

## 五、建议修改

### 方案：将 status 改为 CASE WHEN 动态计算（保持一致）

```java
@Select("SELECT " +
        "camp_id AS id, " +
        "name AS title, " +
        "name AS campName, " +
        "CONCAT('第', term, '期') AS batch, " +
        "intro AS description, " +
        "enroll_count AS participantCount, " +
        "CASE WHEN NOW() < start_time THEN 0 " +
        "WHEN NOW() > end_time THEN 2 " +
        "ELSE 1 END AS status, " +
        "start_time AS startTime, " +
        "end_time AS endTime " +
        "FROM t_camp " +
        "WHERE camp_id = #{id}")
Course selectCourseById(@Param("id") Integer id);
```

---

## 六、前端使用建议

前端在判断"营期是否已结束"时，应优先使用 `endTime` 字段：

```javascript
// 正确方式：使用 endTime 判断
const isCampEnded = new Date() > new Date(course.endTime);

// 不推荐：依赖 status 字段（因为可能是物理字段，不是实时计算的）
const isCampEndedByStatus = course.status === 2;
```

---

## 七、验证步骤

1. 调用 `GET /courses/detail?id=xxx`
2. 检查返回的 JSON 中是否包含 `endTime` 字段
3. 如果没有，检查：
   - Spring Jackson 序列化配置
   - `Course` 类是否有 `@Data` 注解
   - 字段是否为 `private`（正常情况，Lombok 会生成 getter）