# 营期类型字典 & 营期列表查询升级 - API文档

## 功能说明

本次更新包含两个部分：
1. **新增**：营期类型字典接口（用于前端下拉选项）
2. **升级**：营期列表查询接口（新增按体系分类筛选）

---

## 任务一：营期类型字典接口

### 接口契约

- **接口路径**: `GET /api/admin/camp-types/options`
- **请求方式**: GET
- **需要鉴权**: ✅ 是（需要在 Header 中携带 Token）

### 请求头

```
Authorization: Bearer <token>
```

### 响应格式

**成功响应 (200):**

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "typeId": 1,
      "levelName": "明理班"
    },
    {
      "typeId": 2,
      "levelName": "笃行班"
    },
    {
      "typeId": 3,
      "levelName": "诚意班"
    }
  ]
}
```

### 数据库查询

```sql
SELECT 
    type_id AS typeId,
    level_name AS levelName
FROM t_camp_type
ORDER BY type_id
```

---

## 任务二：营期列表查询接口升级

### 接口契约

- **接口路径**: `GET /api/admin/camps`
- **请求方式**: GET
- **需要鉴权**: ✅ 是（需要在 Header 中携带 Token）

### 请求参数

| 参数名 | 必填 | 类型 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `page` | 否 | Integer | 1 | 当前页码（从 1 开始） |
| `size` | 否 | Integer | 10 | 每页大小 |
| `keyword` | 否 | String | - | 关键词，模糊匹配营期名称（LIKE 查询） |
| `status` | 否 | Integer | - | 状态，精确匹配（0-待开课，1-进行中，2-已结束） |
| `typeId` | ❗**新增** | Integer | - | 体系类型 ID，精确匹配（如：1=明理班） |

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
      }
    ]
  }
}
```

### SQL 逻辑

**COUNT 查询：**

```sql
SELECT COUNT(*)
FROM t_camp c
LEFT JOIN t_camp_type ct ON c.type_id = ct.type_id
WHERE 
  (:keyword IS NULL OR :keyword = '' OR c.name LIKE CONCAT('%', :keyword, '%'))
  AND (:status IS NULL OR c.status = :status)
  AND (:typeId IS NULL OR c.type_id = :typeId)
```

**分页查询：**

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
  AND (:typeId IS NULL OR c.type_id = :typeId)
ORDER BY c.start_time DESC
LIMIT :offset, :limit
```

---

## 使用示例

### 示例 1：获取所有营期类型（下拉选项）

```bash
GET http://localhost:8080/api/admin/camp-types/options
Authorization: Bearer <your_token_here>
```

**响应示例：**

```json
{
  "code": 200,
  "data": [
    { "typeId": 1, "levelName": "明理班" },
    { "typeId": 2, "levelName": "笃行班" },
    { "typeId": 3, "levelName": "诚意班" }
  ]
}
```

### 示例 2：按体系类型筛选营期

```bash
GET http://localhost:8080/api/admin/camps?typeId=1
Authorization: Bearer <your_token_here>
```

### 示例 3：组合查询（体系类型 + 状态）

```bash
GET http://localhost:8080/api/admin/camps?typeId=2&status=1
Authorization: Bearer <your_token_here>
```

### 示例 4：完整查询条件

```bash
GET http://localhost:8080/api/admin/camps?page=1&size=20&keyword=线上&typeId=1&status=1
Authorization: Bearer <your_token_here>
```

---

## 前端对接建议

### 1. 页面加载流程

```javascript
// 1. 页面加载时先获取类型选项
const typesResponse = await fetch('/api/admin/camp-types/options', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const types = await typesResponse.json();

// 2. 填充下拉框
types.data.forEach(type => {
  const option = document.createElement('option');
  option.value = type.typeId;
  option.textContent = type.levelName;
  selectElement.appendChild(option);
});

// 3. 监听下拉框变化，触发查询
selectElement.addEventListener('change', (e) => {
  fetchCamps({ typeId: e.target.value });
});
```

### 2. 查询函数示例

```javascript
async function fetchCamps(filters = {}) {
  const params = new URLSearchParams({
    page: 1,
    size: 10,
    ...filters
  });
  
  const response = await fetch(`/api/admin/camps?${params}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  const result = await response.json();
  renderCampList(result.data.list);
  updatePagination(result.data.total);
}

// 使用示例
fetchCamps({ typeId: 1, status: 1 }); // 查询笃行班且进行中的营期
```

---

## 文件清单

### 新增文件

1. **DTO 类**
   - `CampTypeOptionDTO.java` - 营期类型选项 DTO

### 修改文件

1. **Controller**
   - `AdminController.java` - 新增 `/camp-types/options` 接口，升级 `/camps` 接口

2. **Service**
   - `CampService.java` - 新增 `getAllCampTypes()` 方法，升级 `getCampList()` 方法签名
   - `CampServiceImpl.java` - 实现新增方法，升级现有方法

3. **Mapper**
   - `CampMapper.java` - 新增 `selectAllCampTypes()` 方法，升级 `countCampList()` 和 `selectCampList()` 方法签名

4. **Mapper XML**
   - `CampMapper.xml` - 新增 `selectAllCampTypes` SQL，升级 `countCampList` 和 `selectCampList` SQL（添加 typeId 条件）

---

## 测试建议

### 1. 字典接口测试

- ✅ 测试返回数据格式是否正确
- ✅ 测试是否包含所有类型
- ✅ 测试字段映射（typeId, levelName）

### 2. 列表查询升级测试

**基础查询：**
- ✅ 不传 typeId（应返回所有类型）
- ✅ 传入有效 typeId（应只返回该类型）
- ✅ 传入无效 typeId（应返回空列表）

**组合查询：**
- ✅ typeId + keyword
- ✅ typeId + status
- ✅ typeId + keyword + status
- ✅ typeId + 分页

**边界测试：**
- ✅ typeId = null（应忽略该条件）
- ✅ typeId = 0（应返回空或对应数据）

---

## 注意事项

1. **向后兼容性**：`typeId` 为可选参数，不传时查询结果与之前版本一致
2. **联表查询**：COUNT 查询也需要 LEFT JOIN t_camp_type，因为 typeId 条件可能用到
3. **排序规则**：始终按 `start_time DESC` 倒序，不受 typeId 影响
4. **前端对接**：建议先调用字典接口获取选项，再渲染筛选器
