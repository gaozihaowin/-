# 微信小程序课程模块 API 开发报告

## 📋 项目概述

本次开发完成了微信小程序端课程模块的核心功能，包括**今日课程展示**和**任务打卡引擎**两大功能模块。

- **技术栈**: Spring Boot 4.0.2 + MyBatis + MySQL 8.0
- **数据库**: 阿里云云数据库 MySQL 8.0
- **JDK 版本**: Java 21
- **服务端口**: 8080

---

## 🎯 功能模块一：今日课程只读 API

### 接口信息
- **接口地址**: `GET /courses/{campId}/today`
- **请求方式**: GET
- **响应格式**: `Result<TodayCourseDTO>`

### 核心数据契约

#### 响应结构 - TodayCourseDTO
```java
public class TodayCourseDTO {
    private Boolean hasCourse;        // 是否有课
    private String currentDate;       // 当前日期 (M 月 d 日)
    private Integer planId;           // 计划 ID
    private Integer completionRate;   // 完成率百分比
    private List<TaskItemDTO> tasks;  // 任务列表
}
```

#### 任务结构 - TaskItemDTO
```java
public class TaskItemDTO {
    private String taskId;      //read, video, homework, extra1, extra2
    private String taskType;    // FIXED(固定) | EXTRA(备选)
    private String title;       // 任务标题
    private String subtitle;    // 任务副标题
    private Integer isDone;     // 0 未完成 | 1 已完成
}
```

### 业务逻辑

1. **查询今日排课**: 根据营期 ID 和当前日期查询 t_camp_plan 表
2. **兜底处理**: 今日无课时返回 `hasCourse=false` 的空数据
3. **任务组装**: 
   - 固定任务：原文诵读、名师导读、心得打卡（保底 3 个）
   - 备选任务：extra1、extra2（动态添加）
4. **状态同步**: 查询 t_user_daily_record 表获取用户已完成状态
5. **进度计算**: `(已完成数 * 100) / 总任务数`

### 示例响应

#### 场景 1: 今日有课
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "hasCourse": true,
    "currentDate": "3 月 11 日",
    "planId": 10601,
    "completionRate": 33,
    "tasks": [
      {
        "taskId": "read",
        "taskType": "FIXED",
        "title": "原文诵读",
        "subtitle": "论语 · 学而篇",
        "isDone": 1
      },
      {
        "taskId": "video",
        "taskType": "FIXED",
        "title": "名师导读",
        "subtitle": "张三 · 15 分钟深度解析",
        "isDone": 0
      },
      {
        "taskId": "homework",
        "taskType": "FIXED",
        "title": "心得打卡",
        "subtitle": "写下今日感悟",
        "isDone": 0
      }
    ]
  }
}
```

#### 场景 2: 今日无课
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "hasCourse": false,
    "currentDate": "3 月 11 日",
    "planId": null,
    "completionRate": 0,
    "tasks": []
  }
}
```

---

## 🎯 功能模块二：打卡与进度重算引擎

### 接口信息
- **接口地址**: `POST /courses/plan/{planId}/task/complete`
- **请求方式**: POST
- **Content-Type**: application/json
- **响应格式**: `Result<TaskCompleteRespDTO>`

### 核心数据契约

#### 请求参数 - TaskCompleteReqDTO
```java
public class TaskCompleteReqDTO {
    private String taskType;  //read | video | homework | extra1 | extra2
}
```

#### 响应参数 - TaskCompleteRespDTO
```java
public class TaskCompleteRespDTO {
    private Integer planId;         // 计划 ID
    private String taskType;        // 打卡的任务类型
    private Integer completionRate; // 最新完成率百分比
}
```

### 数据库架构红线 ⚠️

**t_user_daily_record 表真实字段**:
- ✅ `record_id` (主键)
- ✅ `user_id` (用户 ID)
- ✅ `camp_id` (营期 ID)
- ✅ `plan_id` (计划 ID)
- ✅ `is_read_done` (诵读状态)
- ✅ `is_video_done` (导读状态)
- ✅ `is_homework_done` (打卡状态)
- ✅ `is_extra1_done` (备选任务 1)
- ✅ `is_extra2_done` (备选任务 2)
- ✅ `completion_rate` (完成率)

**绝对不存在的字段**:
- ❌ `date` - 没有日期字段！
- ❌ `learning_duration` - 没有学习时长！
- ❌ `check_in_status` - 没有签到状态！
- ❌ `extra_task1_name` - 备选任务名称在 t_camp_plan 中！
- ❌ `create_time` - 没有创建时间！

**联合唯一键**: `user_id + plan_id` (通过此组合确定唯一记录)

### 核心业务逻辑

#### 步骤 A: 获取排课与动态分母
```java
CampPlan plan = campPlanMapper.selectById(planId);
int totalTasks = 3; // 保底 3 个任务
if (plan.getExtraTask1Name() != null && !plan.getExtraTask1Name().isEmpty()) {
    totalTasks++;
}
if (plan.getExtraTask2Name() != null && !plan.getExtraTask2Name().isEmpty()) {
    totalTasks++;
}
```

#### 步骤 B: Upsert 机制获取记录
```java
UserDailyRecord record = userDailyRecordMapper.selectByUserIdAndPlanId(userId, planId);
boolean isNew = false;

if (record == null) {
    // 创建新记录
    record = new UserDailyRecord();
    record.setUserId(userId);
    record.setCampId(plan.getCampId());
    record.setPlanId(planId);
    record.setIsReadDone(0);
    record.setIsVideoDone(0);
    record.setIsHomeworkDone(0);
    record.setIsExtra1Done(0);
    record.setIsExtra2Done(0);
    record.setCompletionRate(0);
    isNew = true;
}
```

#### 步骤 C: 状态点亮与重算分子
```java
switch (req.getTaskType()) {
    case "read": record.setIsReadDone(1); break;
    case "video": record.setIsVideoDone(1); break;
    case "homework": record.setIsHomeworkDone(1); break;
    case "extra1": record.setIsExtra1Done(1); break;
    case "extra2": record.setIsExtra2Done(1); break;
}

// 计算已完成数
int completed = 0;
if (record.getIsReadDone() == 1) completed++;
if (record.getIsVideoDone() == 1) completed++;
if (record.getIsHomeworkDone() == 1) completed++;
if (record.getIsExtra1Done() == 1) completed++;
if (record.getIsExtra2Done() == 1) completed++;

// 计算进度
int completionRate = (completed * 100) / totalTasks;
```

#### 步骤 D: 落库与返回
```java
if (isNew) {
    userDailyRecordMapper.insert(record);
} else {
    userDailyRecordMapper.update(record);
}

TaskCompleteRespDTO resp = new TaskCompleteRespDTO();
resp.setPlanId(planId);
resp.setTaskType(req.getTaskType());
resp.setCompletionRate(completionRate);
return resp;
```

### 示例请求与响应

#### 请求示例
```powershell
POST http://localhost:8080/courses/plan/10601/task/complete
Content-Type: application/json

{
  "taskType": "read"
}
```

#### 响应示例
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "planId": 10601,
    "taskType": "read",
    "completionRate": 33
  }
}
```

---

## 🗂️ 文件清单

### 实体类 (Entity)
- ✅ `UserDailyRecord.java` - 用户每日学习记录实体
- ✅ `CampPlan.java` - 排课计划实体（已存在）

### DTO 类
- ✅ `TodayCourseDTO.java` - 今日课程响应 DTO
- ✅ `TaskItemDTO.java` - 任务项 DTO
- ✅ `TaskCompleteReqDTO.java` - 打卡请求 DTO
- ✅ `TaskCompleteRespDTO.java` - 打卡响应 DTO

### Mapper 接口
- ✅ `UserDailyRecordMapper.java` - 用户记录 Mapper
- ✅ `CampPlanMapper.java` - 排课计划 Mapper（已存在）

### Mapper XML
- ✅ `UserDailyRecordMapper.xml` - 用户记录映射文件
- ✅ `CampPlanMapper.xml` - 排课计划映射文件（已存在）

### Service 层
- ✅ `CourseService.java` - 课程服务接口
- ✅ `CourseServiceImpl.java` - 课程服务实现

### Controller 层
- ✅ `AppCourseController.java` - 小程序课程控制器

---

## 🔧 技术亮点

### 1. Upsert 模式实现
通过先查询后判断的方式实现 Upsert：
- 记录不存在 → INSERT
- 记录存在 → UPDATE

### 2. 动态分母计算
总任务数不是固定的，而是根据排课计划中的备选任务动态计算：
- 保底 3 个任务（诵读、导读、打卡）
- 可选 extra1、extra2

### 3. 实时进度重算
每次打卡后立即重新计算完成率并返回最新进度给前端

### 4. 数据库字段严格对齐
所有实体类、Mapper、Service 层代码完全按照真实数据库表结构编写，避免脑补字段

---

## 🚀 测试指南

### 前置准备
1. 确保数据库 t_camp_plan 表有测试数据
2. 确保用户 ID=10001 存在（当前硬编码）

### 测试今日课程接口
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/courses/10601/today" -Method Get | ConvertTo-Json -Depth 10
```

### 测试打卡接口
```powershell
# 测试诵读打卡
$body = @{ taskType = "read" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/courses/plan/10601/task/complete" -Method Post -Body $body -ContentType"application/json" | ConvertTo-Json -Depth 10

# 测试导读打卡
$body = @{ taskType = "video" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/courses/plan/10601/task/complete" -Method Post -Body $body -ContentType"application/json" | ConvertTo-Json -Depth 10

# 测试备选任务 1
$body = @{ taskType = "extra1" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/courses/plan/10601/task/complete" -Method Post -Body $body -ContentType"application/json" | ConvertTo-Json -Depth 10
```

---

## 📊 数据库表结构

### t_camp_plan (排课计划表)
```sql
CREATE TABLE t_camp_plan (
    plan_id INT PRIMARY KEY AUTO_INCREMENT,
    camp_id INT NOT NULL,
    day_index INT NOT NULL,
    plan_date DATE NOT NULL,
    title VARCHAR(255),
    module_index INT,
    module_name VARCHAR(100),
    reading_title VARCHAR(255),
    teacher_name VARCHAR(50),
    video_duration INT,
    extra_task1_name VARCHAR(255),
    extra_task2_name VARCHAR(255)
);
```

### t_user_daily_record (用户每日学习记录表)
```sql
CREATE TABLE t_user_daily_record (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    camp_id INT NOT NULL,
    plan_id INT NOT NULL,
    is_read_done TINYINT DEFAULT 0,
    is_video_done TINYINT DEFAULT 0,
    is_homework_done TINYINT DEFAULT 0,
    is_extra1_done TINYINT DEFAULT 0,
    is_extra2_done TINYINT DEFAULT 0,
    completion_rate INT DEFAULT 0,
    UNIQUE KEY uk_user_plan (user_id, plan_id)
);
```

---

## ⚠️ 避坑指南

### 1. 日期字段陷阱
- ❌ **错误**: 在 t_user_daily_record 中添加 date 字段
- ✅ **正确**: 通过 user_id + plan_id 联合唯一键确定记录

### 2. 字段命名规范
- ❌ **错误**: readStatus, videoStatus
- ✅ **正确**: isReadDone, isVideoDone (与数据库下划线命名对应)

### 3. MyBatis XML 映射
- ❌ **错误**: `<select id="selectById"resultType="...">` (缺少空格)
- ✅ **正确**: `<select id="selectById" resultType="...">`

### 4. 实体类字段一致性
- ❌ **错误**: 实体类包含数据库不存在的字段
- ✅ **正确**: 严格按照数据库表结构定义实体类

---

## 📈 运行状态

- **服务地址**: http://127.0.0.1:8080
- **PID**: 2316
- **启动时间**: 2.5 秒
- **编译状态**: ✅ 106 个源文件编译成功
- **数据库连接**: ✅ 阿里云 MySQL 8.0

---

## 📝 开发日期
2026 年 3 月 11 日

## 👨‍💻 开发者
Java 后端架构师团队
