# 教务排课工作台 API文档

## 概述

本模块提供教务排课工作台的完整后端接口，支持营期下拉选项查询、排课时间轴展示、一键生成日历框架以及单日课表的保存/更新功能。

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
    video_url VARCHAR(500) COMMENT '视频链接',
    graphic_url VARCHAR(500) COMMENT '图文链接',
    INDEX idx_camp_id (camp_id),
    INDEX idx_day_index (day_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课计划表';
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

### 2. 获取某营期的排课时间轴

**接口地址**: `GET /api/admin/camp-plans?campId={campId}`

**接口描述**: 根据营期 ID 查询该营期的所有排课计划，按 day_index 升序排列

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
      "videoUrl": "https://example.com/video1.mp4",
      "graphicUrl": "https://example.com/graphic1.jpg"
    },
    {
      "planId": 2,
      "campId": 106,
      "dayIndex": 2,
      "planDate": "2026-06-21",
      "title": "立志篇导读",
      "videoUrl": "",
      "graphicUrl": ""
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
| videoUrl | String | 视频链接 |
| graphicUrl | String | 图文链接 |

**SQL 动作**:
```sql
SELECT plan_id, camp_id, day_index, plan_date, title, video_url, graphic_url
FROM t_camp_plan
WHERE camp_id = 106
ORDER BY day_index ASC;
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
   - 其他业务字段（title, videoUrl, graphicUrl）留空
5. **批量插入**: 调用 Mapper 批量 INSERT 到 `t_camp_plan`

**SQL 动作**:
```sql
-- 1. 检查是否已存在
SELECT COUNT(*) FROM t_camp_plan WHERE camp_id = 106;

-- 2. 查询营期时间
SELECT start_time, end_time FROM t_camp WHERE camp_id = 106;

-- 3. 批量插入（示例：假设共 3 天）
INSERT INTO t_camp_plan (camp_id, day_index, plan_date, title, video_url, graphic_url)
VALUES
  (106, 1, '2026-06-20', '', '', ''),
  (106, 2, '2026-06-21', '', '', ''),
  (106, 3, '2026-06-22', '', '', '');
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

### 4. 保存/更新单日课表

**接口地址**: `PUT /api/admin/camp-plans`

**接口描述**: 根据 plan_id 更新指定日期的课表内容（标题和资源链接）

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "planId": 1,
  "title": "阳明心学第一课",
  "videoUrl": "https://example.com/video1.mp4",
  "graphicUrl": "https://example.com/graphic1.jpg"
}
```

**请求字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| planId | Integer | 是 | 计划 ID，用于定位记录 |
| title | String | 否 | 导读标题 |
| videoUrl | String | 否 | 视频链接 |
| graphicUrl | String | 否 | 图文链接 |

**响应格式**:
```json
{
  "code": 200,
  "message": "保存成功",
  "data": "保存成功",
  "timestamp": 1709884800000
}
```

**SQL 动作**:
```sql
UPDATE t_camp_plan
SET title = '阳明心学第一课',
    video_url = 'https://example.com/video1.mp4',
    graphic_url = 'https://example.com/graphic1.jpg'
WHERE plan_id = 1;
```

**动态更新策略**: 只更新提供的非空字段，未提供的字段保持不变

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

3. **日期范围无效**:
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
├─────────────────────────┤
│   t_camp_plan           │  ← 数据库表
└─────────────────────────┘
```

### 字段映射关系

| 数据库字段 | Java 实体字段 | DTO 字段 |
|-----------|--------------|----------|
| plan_id | planId | planId |
| camp_id | campId | campId |
| day_index | dayIndex | dayIndex |
| plan_date | planDate | planDate |
| title | title | title |
| video_url | videoUrl | videoUrl |
| graphic_url | graphicUrl | graphicUrl |

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
   - title/videoUrl/graphicUrl: 空字符串
   ↓
6. 批量 INSERT 到数据库
```

---

## 使用示例 (cURL)

### 1. 获取营期下拉选项
```bash
curl -X GET http://localhost:8080/api/admin/camps/options
```

### 2. 获取某营期的排课时间轴
```bash
curl -X GET "http://localhost:8080/api/admin/camp-plans?campId=106"
```

### 3. 一键生成空日历
```bash
curl -X POST http://localhost:8080/api/admin/camp-plans/generate \
  -H "Content-Type: application/json" \
  -d '{"campId":106}'
```

### 4. 保存/更新单日课表
```bash
curl -X PUT http://localhost:8080/api/admin/camp-plans \
  -H "Content-Type: application/json" \
  -d '{
    "planId": 1,
    "title": "阳明心学第一课",
    "videoUrl": "https://example.com/video1.mp4",
    "graphicUrl": "https://example.com/graphic1.jpg"
  }'
```

---

## 注意事项

1. **鉴权要求**: 所有接口均以 `/api/admin` 开头，需要管理员权限认证
2. **事务控制**: `generateCalendar` 方法使用 `@Transactional` 注解，确保批量插入的原子性
3. **日期处理**: 使用 Java 8 的 `LocalDate` 和 `ChronoUnit` 进行日期计算，避免时区问题
4. **防呆设计**: 生成日历前会先检查是否已存在数据，防止重复生成
5. **驼峰命名**: 所有返回给前端的字段均采用驼峰命名规范（campId, dayIndex 等）
6. **空值处理**: 生成的日历框架中，title、videoUrl、graphicUrl 均为空字符串，而非 null

---

## 相关文件清单

- **Entity**: 
  - `Camp.java` - 营期实体类
  - `CampPlan.java` - 排课计划实体类
  
- **DTO**: 
  - `CampOptionDTO.java` - 营期下拉选项 DTO
  - `CampPlanDTO.java` - 排课计划 DTO
  - `GenerateCalendarRequest.java` - 生成日历请求 DTO

- **Mapper**: 
  - `CampMapper.java` - 营期数据访问接口（新增 selectCampOptions 方法）
  - `CampPlanMapper.java` - 排课计划数据访问接口

- **Mapper XML**: 
  - `CampMapper.xml` - 营期 SQL 映射（新增 selectCampOptions 查询）
  - `CampPlanMapper.xml` - 排课计划 SQL 映射

- **Service**: 
  - `CampPlanService.java` - 排课计划服务接口
  - `CampPlanServiceImpl.java` - 排课计划服务实现

- **Controller**: 
  - `CampPlanController.java` - 排课计划 REST 控制器
