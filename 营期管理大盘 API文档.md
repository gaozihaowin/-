# 营期管理大盘 - 分页查询 API文档

## 接口说明

该接口用于 PC 端后台管理系统的【营期管理大盘】页面，支持分页查询营期列表，并可根据关键词和状态进行过滤。

---

## 接口契约

### 基本信息

- **接口路径**: `GET /api/admin/camps`
- **请求方式**: GET
- **需要鉴权**: ✅ 是（需要在 Header 中携带 Token）

---

### 请求参数

#### Query Parameters

| 参数名 | 必填 | 类型 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `page` | 否 | Integer | 1 | 当前页码（从 1 开始） |
| `size` | 否 | Integer | 10 | 每页大小 |
| `keyword` | 否 | String | - | 关键词，模糊匹配营期名称（LIKE 查询） |
| `status` | 否 | Integer | - | 状态，精确匹配（0-待开课，1-进行中，2-已结束） |

#### 请求头

```
Authorization: Bearer <token>
```

---

### 响应格式

**成功响应 (200):**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 45,
    "page": 1,
    "size": 10,
    "list": [
      {
        "campId": 101,
        "typeName": "诚意班",
        "term": 69,
        "name": "【致良知线上课堂】诚意班",
        "startTime": "2025-12-20",
        "endTime": "2026-03-20",
        "status": 1,
        "tag": "热招",
        "enrollCount": 573
      },
      {
        "campId": 102,
        "typeName": "笃行班",
        "term": 25,
        "name": "【致良知线上课堂】笃行班",
        "startTime": "2026-03-01",
        "endTime": "2026-06-01",
        "status": 0,
        "tag": "热招",
        "enrollCount": 5
      }
    ]
  }
}
```

---

## 数据库结构与映射

### 主表 `t_camp` (营期表)

| 字段 | 类型 | 说明 | DTO 映射 |
|------|------|------|---------|
| `camp_id` | INT | 营期 ID | `campId` |
| `type_id` | INT | 类型 ID（关联 t_camp_type） | - |
| `term` | INT | 期数 | `term` |
| `name` | VARCHAR | 营期名称 | `name` |
| `start_time` | DATETIME | 开营时间 | `startTime` (格式化为 yyyy-MM-dd) |
| `end_time` | DATETIME | 结营时间 | `endTime` (格式化为 yyyy-MM-dd) |
| `status` | TINYINT | 状态：0-待开课，1-进行中，2-已结束 | `status` |
| `tag` | VARCHAR | 标签（如：热招） | `tag` |
| `enroll_count` | INT | 报名人数 | `enrollCount` |

### 关联表 `t_camp_type` (体系类型表)

| 字段 | 类型 | 说明 | DTO 映射 |
|------|------|------|---------|
| `type_id` | INT | 类型 ID | - |
| `level_name` | VARCHAR | 等级名称（如：明理班、诚意班） | `typeName` |

---

## 核心 SQL 逻辑

### 1. COUNT 查询（获取总记录数）

```sql
SELECT COUNT(*)
FROM t_camp c
WHERE 
  (:keyword IS NULL OR :keyword = '' OR c.name LIKE CONCAT('%', :keyword, '%'))
  AND (:status IS NULL OR c.status = :status)
```

### 2. 分页查询（联表获取类型名称）

```sql
SELECT 
    c.camp_id,
    ct.level_name AS level_name,
    c.term,
    c.name,
    DATE_FORMAT(c.start_time, '%Y-%m-%d') AS start_time,
    DATE_FORMAT(c.end_time, '%Y-%m-%d') AS end_time,
    c.status,
    c.tag,
    c.enroll_count
FROM t_camp c
LEFT JOIN t_camp_type ct ON c.type_id = ct.type_id
WHERE 
  (:keyword IS NULL OR :keyword = '' OR c.name LIKE CONCAT('%', :keyword, '%'))
  AND (:status IS NULL OR c.status = :status)
ORDER BY c.start_time DESC
LIMIT :offset, :limit
```

**排序规则：**
- 无论是否带查询条件，都按 `t_camp.start_time DESC` 倒序排列

**分页计算：**
- `offset = (page - 1) * size`

---

## 使用示例

### 示例 1：查询第一页（默认 10 条）

```bash
GET http://localhost:8080/api/admin/camps?page=1&size=10
Authorization: Bearer <your_token_here>
```

### 示例 2：搜索包含"诚意"的营期

```bash
GET http://localhost:8080/api/admin/camps?keyword=诚意
Authorization: Bearer <your_token_here>
```

### 示例 3：查询所有"进行中"的营期

```bash
GET http://localhost:8080/api/admin/camps?status=1
Authorization: Bearer <your_token_here>
```

### 示例 4：组合查询（第二页，每页 20 条，搜索"线上"且状态为进行中）

```bash
GET http://localhost:8080/api/admin/camps?page=2&size=20&keyword=线上&status=1
Authorization: Bearer <your_token_here>
```

---

## 业务逻辑说明

1. **连表查询**：使用 `LEFT JOIN t_camp_type` 获取营期类型名称（`typeName`）

2. **时间格式化**：在 SQL 层面使用 `DATE_FORMAT()` 将日期时间格式化为 `yyyy-MM-dd`，舍弃时分秒

3. **动态条件**：
   - `keyword` 参数：对 `t_camp.name` 进行模糊查询（`LIKE '%keyword%'`）
   - `status` 参数：对 `t_camp.status` 进行精确匹配
   - 两个条件都是可选的，支持单独使用或组合使用

4. **分页逻辑**：
   - 先执行 COUNT 查询获取总记录数
   - 再执行分页查询获取当前页数据
   - 返回完整的分页信息（total, page, size, list）

5. **排序规则**：始终按开营时间倒序（`ORDER BY start_time DESC`），确保最新的营期排在前面

---

## 文件清单

### 新增文件

1. **实体类**
   - `CampType.java` - 营期类型实体

2. **DTO 类**
   - `CampListItemDTO.java` - 营期列表项 DTO
   - `CampListPageDTO.java` - 分页响应 DTO

3. **Mapper XML**
   - `CampMapper.xml` - MyBatis 映射文件

### 修改文件

1. **Controller**
   - `AdminController.java` - 新增 `/api/admin/camps` 接口

2. **Service**
   - `CampService.java` - 新增 `getCampList()` 方法
   - `CampServiceImpl.java` - 实现分页查询逻辑

3. **Mapper**
   - `CampMapper.java` - 新增 `countCampList()` 和 `selectCampList()` 方法

---

## 测试建议

1. **基础分页测试**
   - 测试默认参数（page=1, size=10）
   - 测试自定义页码和页大小

2. **条件过滤测试**
   - 单独使用 keyword 搜索
   - 单独使用 status 过滤
   - 组合使用 keyword + status

3. **边界情况测试**
   - 查询空结果（应返回 total=0, list=[]）
   - 查询超过总页数的页码（应返回空列表）

4. **鉴权测试**
   - 未携带 Token（应返回 401）
   - Token 过期（应返回 401）
   - 有效 Token（应正常返回数据）
