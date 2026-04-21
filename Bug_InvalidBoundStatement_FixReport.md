# 🎯 MyBatis Invalid bound statement 错误分析与修复报告

## 📌 问题概述

- **错误信息**: `Invalid bound statement (not found): com.daily.dailychineseculture.mapper.HomeworkMapper.selectMyHomeworkList`
- **触发接口**: `GET /homework/my-list?page=1&size=10`
- **错误级别**: 500 Internal Server Error

---

## 🔍 调用链路追踪

```
前端请求
    ↓
HomeworkController.getMyHomeworkList()  [HomeworkController.java:281]
    ↓
HomeworkServiceImpl.getMyHomeworkPage()  [HomeworkServiceImpl.java:936-943]
    ↓
homeworkMapper.selectMyHomeworkList(userId)  [HomeworkServiceImpl.java:938]  ❌ 报错点
```

---

## ❌ 错误根源分析

### 1. Mapper 接口声明（HomeworkMapper.java:449）

```java
List<MyHomeworkDTO> selectMyHomeworkList(@Param("userId") Long userId);
```

**问题**: 该方法声明后**既没有 `@Select` 注解，也没有 XML 映射**，导致 MyBatis 无法找到对应的 SQL 语句。

### 2. HomeworkMapper.xml 检查结果

| 检查项 | 结果 |
|--------|------|
| `namespace` 配置 | ✅ 正确: `com.daily.dailychineseculture.mapper.HomeworkMapper` |
| `selectMyHomeworkList` SQL 标签 | ❌ **缺失** - XML 中没有定义该 SQL |
| `resultType` 配置 | ❌ 不适用（SQL 本身缺失）|

### 3. Service 层调用（HomeworkServiceImpl.java:936-943）

```java
@Override
public MyHomeworkPageDTO getMyHomeworkPage(Long userId, Integer page, Integer size) {
    PageHelper.startPage(page, size);
    List<MyHomeworkDTO> list = homeworkMapper.selectMyHomeworkList(userId);  // 这里抛出异常
    PageInfo<MyHomeworkDTO> pageInfo = new PageInfo<>(list);
    MyHomeworkPageDTO result = new MyHomeworkPageDTO();
    result.setTotal(pageInfo.getTotal());
    result.setList(pageInfo.getList());
    return result;
}
```

### 4. MyHomeworkDTO 结构

```java
@Data
public class MyHomeworkDTO {
    private Integer homeworkId;          // 作业ID
    private String campName;             // 营地名称 (第X期XX级)
    private String planTitle;            // 计划标题
    private String content;              // 作业内容
    private LocalDateTime submitTime;    // 提交时间
    private Integer isSmallGroupExcellent;  // 小组优秀标记
    private Integer isBigGroupExcellent;    // 大组优秀标记
}
```

---

## 🛠️ 修复方案

### 方案：在 HomeworkMapper.xml 中添加缺失的 SQL 映射

**文件路径**: `src/main/resources/mapper/HomeworkMapper.xml`

**修复位置**: 在 `</mapper>` 结束标签之前添加以下 SQL 语句

```xml
<select id="selectMyHomeworkList" resultType="com.daily.dailychineseculture.dto.MyHomeworkDTO">
    SELECT
        h.homework_id as homeworkId,
        CONCAT('第', camp.term, '期', ct.level_name) as campName,
        cp.title as planTitle,
        h.content as content,
        h.submit_time as submitTime,
        h.is_small_group_excellent as isSmallGroupExcellent,
        h.is_big_group_excellent as isBigGroupExcellent
    FROM t_homework h
    INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id
    INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id
    INNER JOIN t_camp_type ct ON camp.type_id = ct.type_id
    WHERE h.user_id = #{userId}
    ORDER BY h.submit_time DESC
</select>
```

### 修复后的 HomeworkMapper.xml 完整内容

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.daily.dailychineseculture.mapper.HomeworkMapper">

    <select id="countTotalPlansByCamp" resultType="int">
        SELECT COUNT(*)
        FROM t_camp_plan
        WHERE camp_id = #{campId}
    </select>

    <select id="countSubmittedHomeworkByUserAndCamp" resultType="int">
        SELECT COUNT(DISTINCT h.plan_id)
        FROM t_homework h
        JOIN t_camp_plan cp ON h.plan_id = cp.plan_id
        WHERE h.user_id = #{userId}
          AND cp.camp_id = #{campId}
    </select>

    <select id="countMissedConsecutiveDays" resultType="int">
        SELECT COUNT(DISTINCT cp.plan_date)
        FROM t_camp_plan cp
        LEFT JOIN t_homework h
            ON h.plan_id = cp.plan_id
            AND h.user_id = #{userId}
        WHERE cp.camp_id = #{campId}
          AND cp.plan_date &lt; CURDATE()
          AND h.homework_id IS NULL
        ORDER BY cp.plan_date DESC
        LIMIT #{threshold}
    </select>

    <select id="countLateSubmissions" resultType="int">
        SELECT COUNT(DISTINCT cp.plan_id)
        FROM t_camp_plan cp
        JOIN t_homework h ON h.plan_id = cp.plan_id AND h.user_id = #{userId}
        WHERE cp.camp_id = #{campId}
          AND cp.plan_date &lt; CURDATE()
          AND TIME(h.submit_time) > '18:30:00'
    </select>

    <select id="countMissedSubmissions" resultType="int">
        SELECT COUNT(DISTINCT cp.plan_id)
        FROM t_camp_plan cp
        LEFT JOIN t_homework h
            ON h.plan_id = cp.plan_id
            AND h.user_id = #{userId}
        WHERE cp.camp_id = #{campId}
          AND cp.plan_date &lt; CURDATE()
          AND h.homework_id IS NULL
    </select>

    <select id="selectHomeworkIdByUserAndPlan" resultType="int">
        SELECT homework_id
        FROM t_homework
        WHERE user_id = #{userId}
        AND plan_id = #{planId}
        LIMIT 1
    </select>

    <insert id="insertHomework" parameterType="com.daily.dailychineseculture.entity.Homework">
        INSERT INTO t_homework (user_id, plan_id, content, submit_time, is_small_group_excellent, is_big_group_excellent)
        VALUES (#{userId}, #{planId}, #{content}, #{submitTime}, #{isSmallGroupExcellent}, #{isBigGroupExcellent})
    </insert>

    <select id="selectExcellentShowcaseList" resultType="com.daily.dailychineseculture.dto.ExcellentShowcaseDTO">
        SELECT
            h.homework_id as homeworkId,
            COALESCE(u.nickname, u.account, CONCAT('学员', h.user_id)) as authorName,
            u.avatar as avatar,
            CONCAT('第', camp.term, '期', ct.level_name) as campName,
            cp.title as planTitle,
            h.content as content,
            h.submit_time as submitTime
        FROM t_homework h
        INNER JOIN t_user u ON h.user_id = u.user_id
        INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id
        INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id
        INNER JOIN t_camp_type ct ON camp.type_id = ct.type_id
        WHERE (h.is_small_group_excellent = 1 OR h.is_big_group_excellent = 1)
        ORDER BY h.submit_time DESC
    </select>

    <!-- ========== 修复：添加缺失的 selectMyHomeworkList SQL ========== -->
    <select id="selectMyHomeworkList" resultType="com.daily.dailychineseculture.dto.MyHomeworkDTO">
        SELECT
            h.homework_id as homeworkId,
            CONCAT('第', camp.term, '期', ct.level_name) as campName,
            cp.title as planTitle,
            h.content as content,
            h.submit_time as submitTime,
            h.is_small_group_excellent as isSmallGroupExcellent,
            h.is_big_group_excellent as isBigGroupExcellent
        FROM t_homework h
        INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id
        INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id
        INNER JOIN t_camp_type ct ON camp.type_id = ct.type_id
        WHERE h.user_id = #{userId}
        ORDER BY h.submit_time DESC
    </select>

</mapper>
```

---

## ✅ 其他配置检查（均正常）

### 1. application.yml - MyBatis 配置 ✅

```yaml
mybatis:
  configuration:
    map-underscore-to-camel-case: true
  mapper-locations: classpath:mapper/*.xml
```

- `mapper-locations` 正确指向 `classpath:mapper/*.xml`
- `HomeworkMapper.xml` 位于 `src/main/resources/mapper/HomeworkMapper.xml`，路径匹配 ✅

### 2. pom.xml - Maven 构建配置 ✅

- 未发现资源导出问题，`HomeworkMapper.xml` 位于 `resources` 目录下，会被正确打包 ✅

---

## 📋 修复步骤总结

| 步骤 | 操作 | 状态 |
|------|------|------|
| 1 | 在 `HomeworkMapper.xml` 中添加 `selectMyHomeworkList` SQL 映射 | ⬜ 待执行 |
| 2 | 重新编译项目 | ⬜ 待执行 |
| 3 | 重启服务并测试接口 | ⬜ 待验证 |

---

## 📖 错误原因总结

**根本原因**: `HomeworkMapper.java` 接口中声明了 `selectMyHomeworkList` 方法，但该方法**既没有使用 `@Select`/`@Insert` 等注解，也没有在 XML 映射文件中定义对应的 SQL**，导致 MyBatis 在运行时无法找到该语句的定义。

**预防建议**:
1. 在 MyBatis Mapper 接口中声明方法时，要么使用注解定义 SQL，要么确保 XML 中有对应的 SQL 映射
2. 可以使用 MyBatis 官方提供的 `org.apache.ibatis.annotations.*` 注解或 XML 映射两种方式之一，避免混用时遗漏
3. 建议在 CI/CD 流程中加入 MyBatis 绑定检查，早期发现 "not found" 问题
