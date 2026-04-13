# 教务排课工作台 API文档

## 概述

本模块提供教务排课工作台的完整后端接口，支持营期下拉选项查询、排课时间轴展示、一键生成日历框架、单日课表的保存/更新以及排课天数的增加与删除功能。

**更新记录**：
- 2026-03-21：完成一对多架构重构，支持任务管理和排课天数增删
- **2026-04-05：新增智能追加排课接口 `POST /api/admin/camp-plans/add-smart-day`，支持前端周主题智能推算；新增并发防御机制，捕获唯一索引冲突异常**

---

## 数据库表结构

### 1. t_camp (营期表)

```sql
CREATE TABLE t_camp (
    camp_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '营期 ID',
    type_id INT NOT NULL COMMENT '营期类型 ID',
    term INT COMMENT '期数',
    name VARCHAR(100) NOT NULL COMMENT '营期名称',
    intro TEXT COMMENT '营期介绍',
    start_time DATETIME COMMENT '开营时间',
    end_time DATETIME COMMENT '结营时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束',
    enroll_count INT DEFAULT 0 COMMENT '报名人数'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营期表';
```

### 2. t_camp_plan (排课计划表)

```sql
CREATE TABLE t_camp_plan (
    plan_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '计划 ID',
    camp_id INT NOT NULL COMMENT '营期 ID',
    day_index INT NOT NULL COMMENT '第几天',
    plan_date DATE NOT NULL COMMENT '具体日期',
    title VARCHAR(200) COMMENT '导读标题',
    module_index INT COMMENT '模块索引',
    module_name VARCHAR(100) COMMENT '模块名称',
    teacher_name VARCHAR(50) COMMENT '讲师姓名',
    is_finished TINYINT DEFAULT 0 COMMENT '是否完成：0-未完成，1-已完成',
    INDEX idx_camp_id (camp_id),
    INDEX idx_day_index (day_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课计划表';
```

### 3. t_plan_task (任务表) - 新增

```sql
CREATE TABLE t_plan_task (
    task_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '任务 ID',
    plan_id INT NOT NULL COMMENT '排课计划 ID',
    task_type VARCHAR(20) NOT NULL COMMENT '任务类型：VIDEO-视频，READ-阅读，HOMEWORK-作业',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    task_desc VARCHAR(500) COMMENT '任务描述',
    task_url VARCHAR(500) COMMENT '任务链接',
    is_required TINYINT DEFAULT 1 COMMENT '是否必做：1-必修，0-选修',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
    INDEX idx_plan_id (plan_id),
    FOREIGN KEY (plan_id) REFERENCES t_camp_plan(plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';
```

**关联关系**：

```
┌─────────────────┐         ┌─────────────────┐
│  t_camp_plan    │  1 ── N │  t_plan_task    │
├─────────────────┤         ├─────────────────┤
│ plan_id (PK)    │◄────────│ plan_id (FK)    │
│ camp_id         │         │ task_id (PK)    │
│ day_index       │         │ task_type       │
│ plan_date       │         │ task_name       │
│ title           │         │ task_desc       │
│ ...             │         │ task_url        │
└─────────────────┘         │ is_required     │
                            │ sort_order      │
                            │ is_deleted      │
                            └─────────────────┘
```

---

## API接口列表

### 1. 获取营期下拉选项

**接口地址**: `GET /api/admin/camps/options`

**接口描述**: 为排课页顶部下拉框提供数据源，按开营时间倒序返回所有营期的 id 和 name

**请求参数**: 无

**响应格式**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "campId": 106,
      "name": "【致良知线上课堂】诚意班"
    },
    {
      "campId": 104,
      "name": "【致良知线上课堂】良知班"
    }
  ],
  "timestamp": 1709884800000
}
```

**字段说明**:
| 字段 | 类型 | 说明 |
|------|------|------|
| campId | Integer | 营期 ID |
| name | String | 营期名称 |

**SQL 动作**:
```sql
SELECT camp_id AS campId, name
FROM t_camp
ORDER BY start_time DESC;
```

---

### 2. 获取某营期的排课时间轴（已升级）

**接口地址**: `GET /api/admin/camp-plans?campId={campId}`

**接口描述**: 根据营期 ID 查询该营期的所有排课计划，按 day_index 升序排列。每个排课计划包含其下的所有任务列表。

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| campId | Integer | 是 | 营期 ID |

**请求示例**:
```
GET /api/admin/camp-plans?campId=106
```

**响应格式**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "planId": 1,
      "campId": 106,
      "dayIndex": 1,
      "planDate": "2026-06-20",
      "title": "阳明心学第一课",
      "tasks": [
        {
          "taskId": 1001,
          "taskType": "VIDEO",
          "taskName": "导读视频",
          "taskDesc": "请认真观看",
          "taskUrl": "http://localhost:8080/uploads/video1.mp4",
          "isRequired": 1,
          "sortOrder": 1
        },
        {
          "taskId": 1002,
          "taskType": "READ",
          "taskName": "阅读材料",
          "taskDesc": "请阅读以下内容",
          "taskUrl": "http://localhost:8080/uploads/reading.pdf",
          "isRequired": 1,
          "sortOrder": 2
        }
      ]
    },
    {
      "planId": 2,
      "campId": 106,
      "dayIndex": 2,
      "planDate": "2026-06-21",
      "title": "立志篇导读",
      "tasks": []
    }
  ],
  "timestamp": 1709884800000
}
```

**字段说明**:
| 字段 | 类型 | 说明 |
|------|------|------|
| planId | Integer | 计划 ID |
| campId | Integer | 营期 ID |
| dayIndex | Integer | 第几天（从 1 开始） |
| planDate | String | 具体日期（格式：yyyy-MM-dd） |
| title | String | 导读标题 |
| tasks | Array | 任务列表 |
| tasks[].taskId | Integer | 任务 ID（前端新建时传 null） |
| tasks[].taskType | String | 任务类型：VIDEO/READ/HOMEWORK |
| tasks[].taskName | String | 任务名称 |
| tasks[].taskDesc | String | 任务描述 |
| tasks[].taskUrl | String | 任务链接 |
| tasks[].isRequired | Integer | 是否必做：1-必修，0-选修 |
| tasks[].sortOrder | Integer | 排序序号 |

**SQL 动作**:
```sql
-- 1. 查询排课计划列表
SELECT plan_id, camp_id, day_index, plan_date, title, module_index, module_name, teacher_name, is_finished
FROM t_camp_plan
WHERE camp_id = 106
ORDER BY day_index ASC;

-- 2. 遍历每个排课，查询任务列表
SELECT task_id, task_type, task_name, task_desc, task_url, is_required, sort_order
FROM t_plan_task
WHERE plan_id = 1 AND is_deleted = 0
ORDER BY sort_order ASC;
```

---

### 3. 一键生成空日历

**接口地址**: `POST /api/admin/camp-plans/generate`

**接口描述**: 根据营期的开营和结营时间，自动生成完整的排课日历框架

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "campId": 106
}
```

**请求字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| campId | Integer | 是 | 营期 ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "日历框架生成成功",
  "data": "日历框架生成成功",
  "timestamp": 1709884800000
}
```

**核心业务逻辑**:

1. **防呆校验**: 查询 `t_camp_plan`，如果该 campId 已有数据，抛出异常："该营期已存在课表，请勿重复生成"
2. **查询营期信息**: 根据 campId 查询 `t_camp` 的 `start_time` 和 `end_time`
3. **计算天数**: 通过 Java LocalDate 计算两个日期之间的天数（含起止日）
4. **循环生成**: 
   - `day_index` 从 1 开始递增
   - `plan_date` 依次向后加一天
   - 其他业务字段（title）留空
5. **批量插入**: 调用 Mapper 批量 INSERT 到 `t_camp_plan`

**SQL 动作**:
```sql
-- 1. 检查是否已存在
SELECT COUNT(*) FROM t_camp_plan WHERE camp_id = 106;

-- 2. 查询营期时间
SELECT start_time, end_time FROM t_camp WHERE camp_id = 106;

-- 3. 批量插入（示例：假设共 3 天）
INSERT INTO t_camp_plan (camp_id, day_index, plan_date, title, module_index, module_name, teacher_name, is_finished)
VALUES
  (106, 1, '2026-06-20', NULL, NULL, NULL, NULL, 0),
  (106, 2, '2026-06-21', NULL, NULL, NULL, NULL, 0),
  (106, 3, '2026-06-22', NULL, NULL, NULL, NULL, 0);
```

**错误响应**:
```json
{
  "code": 500,
  "message": "该营期已存在课表，请勿重复生成",
  "data": null,
  "timestamp": 1709884800000
}
```

---

### 4. 新增一天的排课（新增接口）

**接口地址**: `POST /api/admin/camp-plans`

**接口描述**: 手动添加一天的排课，传入 campId, dayIndex, planDate 等基本信息，插入 t_camp_plan

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "campId": 106,
  "dayIndex": 4,
  "planDate": "2026-06-23",
  "title": "新增课程"
}
```

**请求字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| campId | Integer | 是 | 营期 ID |
| dayIndex | Integer | 是 | 第几天 |
| planDate | String | 是 | 具体日期（格式：yyyy-MM-dd） |
| title | String | 否 | 导读标题 |

**响应格式**:
```json
{
  "code": 200,
  "message": "新增成功",
  "data": {
    "planId": 10,
    "campId": 106,
    "dayIndex": 4,
    "planDate": "2026-06-23",
    "title": "新增课程",
    "tasks": []
  },
  "timestamp": 1709884800000
}
```

**SQL 动作**:
```sql
INSERT INTO t_camp_plan (camp_id, day_index, plan_date, title, module_index, module_name, teacher_name, is_finished)
VALUES (106, 4, '2026-06-23', '新增课程', NULL, NULL, NULL, 0);
```

---

### 5. 保存/更新单日课表（已升级）

**接口地址**: `PUT /api/admin/camp-plans`

**接口描述**: 根据 plan_id 更新指定日期的课表内容（标题和任务列表）。采用全量同步策略，自动处理任务的新增、更新和删除。

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "planId": 1,
  "title": "阳明心学第一课",
  "tasks": [
    {
      "taskId": 1001,
      "taskType": "VIDEO",
      "taskName": "导读视频",
      "taskDesc": "请认真观看",
      "taskUrl": "http://localhost:8080/uploads/video1.mp4",
      "isRequired": 1,
      "sortOrder": 1
    },
    {
      "taskId": null,
      "taskType": "READ",
      "taskName": "阅读材料",
      "taskDesc": "请阅读以下内容",
      "taskUrl": "http://localhost:8080/uploads/reading.pdf",
      "isRequired": 1,
      "sortOrder": 2
    }
  ]
}
```

**请求字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| planId | Integer | 是 | 计划 ID，用于定位记录 |
| title | String | 否 | 导读标题 |
| tasks | Array | 否 | 任务列表 |
| tasks[].taskId | Integer | 否 | 任务 ID（新建任务传 null） |
| tasks[].taskType | String | 是 | 任务类型：VIDEO/READ/HOMEWORK |
| tasks[].taskName | String | 是 | 任务名称 |
| tasks[].taskDesc | String | 否 | 任务描述 |
| tasks[].taskUrl | String | 否 | 任务链接 |
| tasks[].isRequired | Integer | 否 | 是否必做：1-必修，0-选修 |
| tasks[].sortOrder | Integer | 否 | 排序序号 |

**响应格式**:
```json
{
  "code": 200,
  "message": "保存成功",
  "data": "保存成功",
  "timestamp": 1709884800000
}
```

**全量同步逻辑**:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        任务全量同步流程                                  │
├─────────────────────────────────────────────────────────────────────────┤
│  1. 查询数据库中该 planId 原有的所有任务 ID                              │
│     SELECT task_id FROM t_plan_task WHERE plan_id = ? AND is_deleted = 0│
│                                                                         │
│  2. 收集前端传来的有效 taskId（非 null）                                 │
│                                                                         │
│  3. 比对找出需要删除的任务 ID                                            │
│     ┌─────────────────┐     ┌─────────────────┐                        │
│     │ 数据库原有 ID   │  -  │ 前端传来 ID     │  = 需删除的 ID         │
│     └─────────────────┘     └─────────────────┘                        │
│                                                                         │
│  4. 批量删除不再需要的任务                                               │
│     DELETE FROM t_plan_task WHERE task_id IN (?, ?, ...)               │
│                                                                         │
│  5. 遍历前端传来的任务：                                                 │
│     - taskId == null → INSERT（新增）                                   │
│     - taskId != null → UPDATE（更新）                                   │
└─────────────────────────────────────────────────────────────────────────┘
```

**SQL 动作**:
```sql
-- 1. 更新排课基本信息
UPDATE t_camp_plan SET title = '阳明心学第一课' WHERE plan_id = 1;

-- 2. 查询原有任务 ID
SELECT task_id FROM t_plan_task WHERE plan_id = 1 AND is_deleted = 0;

-- 3. 删除不再需要的任务（如果有）
DELETE FROM t_plan_task WHERE task_id IN (1003, 1004);

-- 4. 更新已有任务
UPDATE t_plan_task 
SET task_type = 'VIDEO', 
    task_name = '导读视频', 
    task_desc = '请认真观看',
    task_url = 'http://localhost:8080/uploads/video1.mp4',
    is_required = 1,
    sort_order = 1
WHERE task_id = 1001;

-- 5. 新增任务
INSERT INTO t_plan_task (plan_id, task_type, task_name, task_desc, task_url, is_required, sort_order, is_deleted)
VALUES (1, 'READ', '阅读材料', '请阅读以下内容', 'http://localhost:8080/uploads/reading.pdf', 1, 2, 0);
```

---

### 6. 删除整天排课及任务（新增接口）

**接口地址**: `DELETE /api/admin/camp-plans/{planId}`

**接口描述**: 删除指定的排课计划及其下挂载的所有任务

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| planId | Integer | 是 | 排课 ID（路径参数） |

**请求示例**:
```
DELETE /api/admin/camp-plans/1
```

**响应格式**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": "删除成功",
  "timestamp": 1709884800000
}
```

**SQL 动作**:
```sql
-- 1. 先删除该排课下的所有任务
DELETE FROM t_plan_task WHERE plan_id = 1;

-- 2. 再删除排课计划
DELETE FROM t_camp_plan WHERE plan_id = 1;
```

**错误响应**:
```json
{
  "code": 500,
  "message": "未找到指定的排课计划",
  "data": null,
  "timestamp": 1709884800000
}
```

---

### 7. 智能追加一天排课（★ 新增核心接口）

**接口地址**: `POST /api/admin/camp-plans/add-smart-day`

**接口描述**: 前端智能推算完整排课数据后，后端仅负责落库。支持周主题智能继承，并具备并发防御机制。

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "campId": 101,
  "dayIndex": 8,
  "planDate": "2026-03-08",
  "moduleName": "第二周：知行合一",
  "moduleIndex": 2,
  "teacherName": "博仁老师",
  "title": "第8课：知行本体"
}
```

**请求字段说明**:
| 字段 | 类型 | 必填 | 校验注解 | 说明 |
|------|------|------|----------|------|
| campId | Integer | 是 | `@NotNull` | 营期 ID |
| dayIndex | Integer | 是 | `@NotNull`, `@Min(1)` | 第几天，必须大于0 |
| planDate | String | 是 | `@NotNull` | 具体日期（格式：yyyy-MM-dd） |
| moduleName | String | 是 | `@NotBlank` | 模块名称（如"第二周：知行合一"） |
| moduleIndex | Integer | 是 | `@NotNull` | 模块索引（第几周） |
| teacherName | String | 否 | 无 | 讲师姓名 |
| title | String | 是 | `@NotBlank` | 导读主题 |

**响应格式（成功）**:
```json
{
  "code": 200,
  "message": "智能追加排课成功",
  "data": null,
  "timestamp": 1743772800000
}
```

**响应格式（并发冲突 - 409）**:
```json
{
  "code": 409,
  "message": "该营期的当前天数或日期已被占用，请刷新页面获取最新排课进度！",
  "data": null,
  "timestamp": 1743772800000
}
```

**核心业务逻辑**:

1. **营期校验**: 验证 campId 对应的营期是否存在
2. **属性拷贝**: 使用 `BeanUtils.copyProperties` 将 DTO 属性拷贝到实体
3. **默认值设置**: 强制设置 `isFinished = 0`（默认未完成）
4. **铁桶防御**: 捕获 `DuplicateKeyException`（数据库唯一索引冲突），返回友好错误提示

**数据库唯一索引约束**:

| 索引名 | 字段 | 用途 |
|--------|------|------|
| `uk_camp_day` | `camp_id`, `day_index` | 防止同一营期重复天数 |
| `uk_camp_date` | `camp_id`, `plan_date` | 防止同一营期重复日期 |

**SQL 动作**:
```sql
INSERT INTO t_camp_plan (camp_id, day_index, plan_date, module_name, module_index, teacher_name, title, is_finished)
VALUES (101, 8, '2026-03-08', '第二周：知行合一', 2, '博仁老师', '第8课：知行本体', 0);
```

**cURL 示例**:
```bash
curl -X POST http://localhost:8080/api/admin/camp-plans/add-smart-day \
  -H "Content-Type: application/json" \
  -d '{
    "campId": 101,
    "dayIndex": 8,
    "planDate": "2026-03-08",
    "moduleName": "第二周：知行合一",
    "moduleIndex": 2,
    "teacherName": "博仁老师",
    "title": "第8课：知行本体"
  }'
```

---

## 数据契约（前后端统一 JSON 示例）

### 完整的 CampPlanDTO 结构

```json
{
  "planId": 123,
  "campId": 1,
  "dayIndex": 1,
  "planDate": "2026-06-20",
  "title": "第一课：阳明心学导论",
  "tasks": [
    {
      "taskId": 1001,
      "taskType": "VIDEO",
      "taskName": "导读视频",
      "taskDesc": "请认真观看",
      "taskUrl": "http://localhost:8080/uploads/xxx.mp4",
      "isRequired": 1,
      "sortOrder": 1
    },
    {
      "taskId": 1002,
      "taskType": "READ",
      "taskName": "阅读材料",
      "taskDesc": "请阅读以下内容",
      "taskUrl": "http://localhost:8080/uploads/reading.pdf",
      "isRequired": 1,
      "sortOrder": 2
    },
    {
      "taskId": null,
      "taskType": "HOMEWORK",
      "taskName": "课后作业",
      "taskDesc": "请完成以下作业",
      "taskUrl": null,
      "isRequired": 0,
      "sortOrder": 3
    }
  ]
}
```

### 任务类型枚举

| 值 | 说明 |
|------|------|
| `VIDEO` | 视频任务 |
| `READ` | 阅读任务 |
| `HOMEWORK` | 作业任务 |

### 必修/选修枚举

| 值 | 说明 |
|------|------|
| `1` | 必修 |
| `0` | 选修 |

---

## 错误响应

**通用错误格式**:
```json
{
  "code": 500,
  "message": "错误描述信息",
  "data": null,
  "timestamp": 1709884800000
}
```

**常见错误场景**:

1. **重复生成日历**:
   ```json
   {
     "code": 500,
     "message": "该营期已存在课表，请勿重复生成",
     "data": null
   }
   ```

2. **营期不存在**:
   ```json
   {
     "code": 500,
     "message": "未找到指定的营期",
     "data": null
   }
   ```

3. **排课不存在**:
   ```json
   {
     "code": 500,
     "message": "未找到指定的排课计划",
     "data": null
   }
   ```

4. **日期范围无效**:
   ```json
   {
     "code": 500,
     "message": "开营时间必须早于或等于结营时间",
     "data": null
   }
   ```

---

## 技术实现细节

### 分层架构

```
┌─────────────────────────┐
│  CampPlanController     │  ← Controller 层 (REST API)
├─────────────────────────┤
│  CampPlanService        │  ← Service 层 (业务逻辑)
├─────────────────────────┤
│ CampPlanMapper          │  ← Mapper 层 (数据访问)
│ PlanTaskMapper          │
├─────────────────────────┤
│   t_camp_plan           │  ← 数据库表
│   t_plan_task           │
└─────────────────────────┘
```

### 字段映射关系

**CampPlanDTO 字段映射**:

| 数据库字段 | Java 实体字段 | DTO 字段 |
|-----------|--------------|----------|
| plan_id | planId | planId |
| camp_id | campId | campId |
| day_index | dayIndex | dayIndex |
| plan_date | planDate | planDate |
| title | title | title |

**TaskAdminDTO 字段映射**:

| 数据库字段 | Java 实体字段 | DTO 字段 |
|-----------|--------------|----------|
| task_id | taskId | taskId |
| plan_id | planId | - (不暴露给前端) |
| task_type | taskType | taskType |
| task_name | taskName | taskName |
| task_desc | taskDesc | taskDesc |
| task_url | taskUrl | taskUrl |
| is_required | isRequired | isRequired |
| sort_order | sortOrder | sortOrder |

### 核心业务逻辑

**一键生成日历流程**:
```
1. 接收 campId
   ↓
2. 校验是否已存在排课计划
   ↓
3. 查询营期的 start_time 和 end_time
   ↓
4. 计算总天数 = (endDate - startDate) + 1
   ↓
5. 循环生成 Plan 对象
   - dayIndex: 1, 2, 3, ...
   - planDate: startDate, startDate+1, startDate+2, ...
   - title: NULL
   ↓
6. 批量 INSERT 到数据库
```

**任务全量同步流程**:
```
1. 接收 CampPlanDTO（含 tasks 列表）
   ↓
2. 更新 t_camp_plan 的 title 等基本信息
   ↓
3. 全量同步任务列表：
   a. 查询数据库中原有任务 ID
   b. 比对找出需要删除的任务 ID
   c. 批量删除不再需要的任务
   d. 遍历任务：
      - taskId == null → INSERT
      - taskId != null → UPDATE
```

---

## 使用示例 (cURL)

### 1. 获取营期下拉选项
```bash
curl -X GET http://localhost:8080/api/admin/camps/options
```

### 2. 获取某营期的排课时间轴（含任务）
```bash
curl -X GET "http://localhost:8080/api/admin/camp-plans?campId=106"
```

### 3. 一键生成空日历
```bash
curl -X POST http://localhost:8080/api/admin/camp-plans/generate \
  -H "Content-Type: application/json" \
  -d '{"campId":106}'
```

### 4. 新增一天的排课
```bash
curl -X POST http://localhost:8080/api/admin/camp-plans \
  -H "Content-Type: application/json" \
  -d '{
    "campId": 106,
    "dayIndex": 4,
    "planDate": "2026-06-23",
    "title": "新增课程"
  }'
```

### 5. 保存/更新单日课表（含任务全量同步）
```bash
curl -X PUT http://localhost:8080/api/admin/camp-plans \
  -H "Content-Type: application/json" \
  -d '{
    "planId": 1,
    "title": "阳明心学第一课",
    "tasks": [
      {
        "taskId": 1001,
        "taskType": "VIDEO",
        "taskName": "导读视频",
        "taskDesc": "请认真观看",
        "taskUrl": "http://localhost:8080/uploads/video1.mp4",
        "isRequired": 1,
        "sortOrder": 1
      },
      {
        "taskId": null,
        "taskType": "READ",
        "taskName": "阅读材料",
        "taskDesc": "请阅读以下内容",
        "taskUrl": "http://localhost:8080/uploads/reading.pdf",
        "isRequired": 1,
        "sortOrder": 2
      }
    ]
  }'
```

### 6. 删除整天排课及任务
```bash
curl -X DELETE http://localhost:8080/api/admin/camp-plans/1
```

---

## 注意事项

1. **鉴权要求**: 所有接口均以 `/api/admin` 开头，需要管理员权限认证
2. **事务控制**: `generateCalendar`、`saveOrUpdateCampPlan`、`deleteCampPlan` 方法使用 `@Transactional` 注解，确保操作的原子性
3. **日期处理**: 使用 Java 8 的 `LocalDate` 和 `ChronoUnit` 进行日期计算，避免时区问题
4. **防呆设计**: 生成日历前会先检查是否已存在数据，防止重复生成
5. **驼峰命名**: 所有返回给前端的字段均采用驼峰命名规范（campId, dayIndex 等）
6. **空值处理**: 生成的日历框架中，title 为 NULL，tasks 为空数组
7. **任务全量同步**: PUT 接口采用全量同步策略，前端需传递完整的任务列表
8. **taskId 为 null**: 表示新增任务，后端会自动生成 ID 并回填

---

## 相关文件清单

- **Entity**: 
  - `Camp.java` - 营期实体类
  - `CampPlan.java` - 排课计划实体类
  - `PlanTask.java` - 任务实体类（新增）
  
- **DTO**: 
  - `CampOptionDTO.java` - 营期下拉选项 DTO
  - `CampPlanDTO.java` - 排课计划 DTO（已更新，新增 tasks 字段）
  - `TaskAdminDTO.java` - 任务管理 DTO（新增）
  - `GenerateCalendarRequest.java` - 生成日历请求 DTO

- **Mapper**: 
  - `CampMapper.java` - 营期数据访问接口
  - `CampPlanMapper.java` - 排课计划数据访问接口（已更新）
  - `PlanTaskMapper.java` - 任务数据访问接口（已更新）

- **Mapper XML**: 
  - `CampMapper.xml` - 营期 SQL 映射
  - `CampPlanMapper.xml` - 排课计划 SQL 映射（已更新）
  - `PlanTaskMapper.xml` - 任务 SQL 映射（已更新）

- **Service**: 
  - `CampPlanService.java` - 排课计划服务接口（已更新）
  - `CampPlanServiceImpl.java` - 排课计划服务实现（已重构）

- **Controller**: 
  - `CampPlanController.java` - 排课计划 REST 控制器（已更新）
