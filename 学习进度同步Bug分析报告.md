# 学习进度同步 Bug 分析报告

## 一、基础环境信息

- **基础包路径**：`com.daily.dailychineseculture`
- **项目路径**：`c:\Users\chenxiao\testProject\JavaTongyi\daily-chinese-studies`

---

## 二、核心实体类 (Entity)

### 2.1 CampEnrollment（学员报名实体）

**文件路径**：`src/main/java/com/daily/dailychineseculture/entity/CampEnrollment.java`

**说明**：代码库中未找到 `CampEnrollment` 实体类文件，但根据 `CampEnrollmentMapper.xml` 推断表结构如下：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 主键 |
| user_id | Long | 用户ID |
| camp_id | Integer | 营期ID |
| is_completed | Integer | 是否完成（0/1） |
| progress | Integer | 学习进度百分比（0-100） |

### 2.2 UserDailyRecord（用户每日学习记录实体）

**文件路径**：`src/main/java/com/daily/dailychineseculture/entity/UserDailyRecord.java`

```java
package com.daily.dailychineseculture.entity;

import lombok.Data;

/**
 * 用户每日学习记录实体类
 * 对应数据库表：t_user_daily_record
 */
@Data
public class UserDailyRecord {
    /**
     * 记录 ID (主键)
     */
    private Integer recordId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 营期 ID
     */
    private Integer campId;

    /**
     * 计划 ID
     */
    private Integer planId;
    
    /**
     * 完成率 (0-100)
     */
    private Integer completionRate;

    /**
     * 是否全部完成：0-未完成，1-已完成
     */
    private Integer isAllCompleted;
}
```

### 2.3 CampPlan（排课计划实体）

**文件路径**：`src/main/java/com/daily/dailychineseculture/entity/CampPlan.java`

```java
package com.daily.dailychineseculture.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;
import java.util.Date;

/**
 * 排课计划实体类
 * 对应数据库表：t_camp_plan
 */
@Data
@Alias("CampPlan")
public class CampPlan {
    /**
     * 计划 ID
     */
    private Integer planId;
    
    /**
     * 营期 ID
     */
    private Integer campId;
    
    /**
     * 第几天
     */
    private Integer dayIndex;
    
    /**
     * 具体日期
     */
    private Date planDate;
    
    /**
     * 导读标题
     */
    private String title;
    
    /**
     * 模块索引（第几周）
     */
    private Integer moduleIndex;
    
    /**
     * 模块名称
     */
    private String moduleName;
    
    /**
     * 讲师姓名
     */
    private String teacherName;
    
    /**
     * 是否完成：0-未完成，1-已完成
     */
    private Integer isFinished;
}
```

---

## 三、持久层 (Mapper)

### 3.1 CampEnrollmentMapper.java

**文件路径**：`src/main/java/com/daily/dailychineseculture/mapper/CampEnrollmentMapper.java`

```java
package com.daily.dailychineseculture.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CampEnrollmentMapper {
    Integer countByUserIdAndCampId(@Param("userId") Long userId, @Param("campId") Integer campId);

    int insertEnrollment(@Param("userId") Long userId, @Param("campId") Integer campId);
}
```

### 3.2 CampEnrollmentMapper.xml

**文件路径**：`src/main/resources/mapper/CampEnrollmentMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.daily.dailychineseculture.mapper.CampEnrollmentMapper">
    <select id="countByUserIdAndCampId" resultType="int">
        SELECT COUNT(1)
        FROM t_camp_enrollment
        WHERE user_id = #{userId}
          AND camp_id = #{campId}
    </select>

    <insert id="insertEnrollment">
        INSERT INTO t_camp_enrollment (
            user_id,
            camp_id,
            is_completed,
            progress
        ) VALUES (
            #{userId},
            #{campId},
            0,
            0
        )
    </insert>
</mapper>
```

**注意**：`CampEnrollmentMapper` 中**缺少 `updateProgress` 方法**，无法更新 `t_camp_enrollment` 表中的 `progress` 字段。

### 3.3 UserDailyRecordMapper.java

**文件路径**：`src/main/java/com/daily/dailychineseculture/mapper/UserDailyRecordMapper.java`

```java
package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.UserDailyRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户每日学习记录 Mapper
 */
@Mapper
public interface UserDailyRecordMapper {

    /**
     * 根据 ID 查询记录
     */
    @Select("SELECT record_id, user_id, camp_id, plan_id, completion_rate, is_all_completed FROM t_user_daily_record WHERE record_id = #{recordId}")
    UserDailyRecord selectById(Integer recordId);

    /**
     * 根据用户 ID 和计划 ID 查询记录（user_id + plan_id 联合唯一）
     */
    @Select("SELECT record_id, user_id, camp_id, plan_id, completion_rate, is_all_completed FROM t_user_daily_record WHERE user_id = #{userId} AND plan_id = #{planId}")
    UserDailyRecord selectByUserIdAndPlanId(@Param("userId") Long userId, @Param("planId") Integer planId);
    
    /**
     * 根据用户 ID 和营期 ID 查询所有打卡记录
     */
    @Select("SELECT record_id, user_id, camp_id, plan_id, completion_rate, is_all_completed FROM t_user_daily_record WHERE user_id = #{userId} AND camp_id = #{campId}")
    List<UserDailyRecord> selectByUserIdAndCampId(@Param("userId") Long userId, @Param("campId") Integer campId);

    /**
     * 插入或更新汇总记录
     */
    @Insert("INSERT INTO t_user_daily_record(user_id, camp_id, plan_id, completion_rate, is_all_completed) " +
            "VALUES(#{userId}, #{campId}, #{planId}, #{completionRate}, #{isAllCompleted}) " +
            "ON DUPLICATE KEY UPDATE completion_rate = #{completionRate}, is_all_completed = #{isAllCompleted}")
    int upsertSummary(@Param("userId") Long userId,
                      @Param("campId") Integer campId,
                      @Param("planId") Integer planId,
                      @Param("completionRate") Integer completionRate,
                      @Param("isAllCompleted") Integer isAllCompleted);
}
```

---

## 四、业务触发点 (Service)

### 4.1 完成任务核心方法 - CourseServiceImpl.completeTask()

**文件路径**：`src/main/java/com/daily/dailychineseculture/service/impl/CourseServiceImpl.java`

**完整代码**（第 221-261 行）：

```java
@Override
@Transactional(rollbackFor = Exception.class)
public TaskCompleteRespDTO completeTask(Integer planId, TaskCompleteReqDTO req, Long currentUserId) {
    if (req == null || req.getTaskId() == null) {
        throw new IllegalArgumentException("taskId 不能为空");
    }

    CampPlan plan = campPlanMapper.selectById(planId);
    if (plan == null) {
        throw new IllegalArgumentException("排课计划不存在，planId: " + planId);
    }

    Integer taskId = req.getTaskId();
    Integer exists = planTaskMapper.countTaskInPlan(planId, taskId);
    if (exists == null || exists <= 0) {
        throw new IllegalArgumentException("任务不存在或不属于该排课，taskId: " + taskId);
    }

    userTaskRecordMapper.upsertDoneRecord(currentUserId, planId, taskId);

    Integer totalRequired = planTaskMapper.countRequiredTasksByPlanId(planId);
    Integer completedRequired = planTaskMapper.countCompletedRequiredTasksByUserIdAndPlanId(currentUserId, planId);
    int total = totalRequired == null ? 0 : totalRequired;
    int completed = completedRequired == null ? 0 : completedRequired;

    int newRate = 0;
    if (total > 0) {
        newRate = (completed * 100) / total;
    } else {
        newRate = 100;
    }
    int isAllCompleted = (newRate == 100) ? 1 : 0;

    userDailyRecordMapper.upsertSummary(currentUserId, plan.getCampId(), planId, newRate, isAllCompleted);

    TaskCompleteRespDTO resp = new TaskCompleteRespDTO();
    resp.setPlanId(planId);
    resp.setTaskType(planTaskMapper.selectTaskTypeByTaskId(taskId));
    resp.setCompletionRate(newRate);
    return resp;
}
```

---

## 五、Bug 分析与缺口

### 5.1 当前逻辑链路

```
用户完成任务 → completeTask() 
    → userTaskRecordMapper.upsertDoneRecord()     ✅ 记录单个任务完成
    → 计算完成率 newRate
    → userDailyRecordMapper.upsertSummary()       ✅ 更新 t_user_daily_record 表
```

### 5.2 缺失的逻辑

**当前 `completeTask()` 方法中，只更新了 `t_user_daily_record`（单日进度），但没有同步更新 `t_camp_enrollment.progress`（营期总进度）。**

```
缺失环节：
    → ❌ 缺少：更新 t_camp_enrollment.progress 的逻辑
```

### 5.3 修复建议

需要在 `completeTask()` 方法末尾（或新增专门方法），增加：

1. **在 `CampEnrollmentMapper` 中新增方法**：
```java
int updateProgress(@Param("userId") Long userId, @Param("campId") Integer campId, @Param("progress") Integer progress);
```

2. **在 `CourseServiceImpl.completeTask()` 中调用**：
```java
// 计算营期总进度（基于所有 plan 的完成率平均值）
List<UserDailyRecord> allRecords = userDailyRecordMapper.selectByUserIdAndCampId(currentUserId, plan.getCampId());
int overallProgress = 0;
if (allRecords != null && !allRecords.isEmpty()) {
    int sum = allRecords.stream().mapToInt(r -> r.getCompletionRate() != null ? r.getCompletionRate() : 0).sum();
    overallProgress = sum / allRecords.size();
}
campEnrollmentMapper.updateProgress(currentUserId, plan.getCampId(), overallProgress);
```

---

## 六、总结

| 项目 | 状态 |
|------|------|
| `t_camp_enrollment` 表存在 `progress` 字段 | ✅ 是 |
| `CampEnrollmentMapper` 有 `updateProgress` 方法 | ❌ **缺失** |
| `completeTask()` 中同步更新 `progress` | ❌ **缺失** |
| `t_user_daily_record` 记录单日进度 | ✅ 正常 |
| `t_user_task_record` 记录单任务完成 | ✅ 正常 |