# 课程体系分类管理 API文档

## 概述

本模块提供对 `t_camp_type`（体系分类表）的完整 CRUD 操作。由于是基础字典表，数据量极小，所有接口均返回全量数据，无需分页。

---

## API接口列表

### 1. 查询全量列表

**接口地址**: `GET /api/admin/camp-types`

**接口描述**: 查询所有课程体系分类，按 type_id 升序排列

**请求参数**: 无

**响应格式**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "typeId": 1,
      "level": "ML",
      "levelName": "明理班"
    },
    {
      "typeId": 2,
      "level": "DX",
      "levelName": "笃行班"
    },
    {
      "typeId": 3,
      "level": "CY",
      "levelName": "诚意班"
    }
  ],
  "timestamp": 1709884800000
}
```

**字段说明**:
| 字段 | 类型 | 说明 |
|------|------|------|
| typeId | Integer | 类型 ID，主键 |
| level | String | 等级标识（如：ML、DX、CY） |
| levelName | String | 等级名称（如：明理班、笃行班） |

---

### 2. 新增体系分类

**接口地址**: `POST /api/admin/camp-types`

**接口描述**: 新增一个课程体系分类

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "level": "CY",
  "levelName": "诚意班"
}
```

**请求字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| level | String | 是 | 等级标识（如：ML、DX、CY） |
| levelName | String | 是 | 等级名称 |

**响应格式**:
```json
{
  "code": 200,
  "message": "新增成功",
  "data": "新增成功",
  "timestamp": 1709884800000
}
```

**SQL 动作**:
```sql
INSERT INTO t_camp_type (level, level_name)
VALUES ('CY', '诚意班');
```

---

### 3. 修改体系分类

**接口地址**: `PUT /api/admin/camp-types`

**接口描述**: 修改指定的课程体系分类

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "typeId": 1,
  "level": "ML-UPDATE",
  "levelName": "明理班 (新)"
}
```

**请求字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| typeId | Integer | 是 | 类型 ID，用于定位记录 |
| level | String | 否 | 等级标识 |
| levelName | String | 否 | 等级名称 |

**响应格式**:
```json
{
  "code": 200,
  "message": "修改成功",
  "data": "修改成功",
  "timestamp": 1709884800000
}
```

**SQL 动作**:
```sql
UPDATE t_camp_type
SET level = 'ML-UPDATE',
    level_name = '明理班 (新)'
WHERE type_id = 1;
```

---

### 4. 删除体系分类

**接口地址**: `DELETE /api/admin/camp-types/{typeId}`

**接口描述**: 删除指定的课程体系分类

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| typeId | Integer | 类型 ID |

**请求示例**:
```
DELETE /api/admin/camp-types/1
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
DELETE FROM t_camp_type
WHERE type_id = 1;
```

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

**常见错误码**:
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权访问
- `404`: 资源不存在
- `500`: 服务器内部错误

---

## 技术实现细节

### 数据库表结构

```sql
CREATE TABLE t_camp_type (
    type_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '类型 ID',
    level VARCHAR(50) NOT NULL COMMENT '等级标识',
    level_name VARCHAR(100) NOT NULL COMMENT '等级名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体系分类表';
```

### 字段映射关系

| 数据库字段 | Java 实体字段 | DTO 字段 |
|-----------|--------------|----------|
| type_id | typeId | typeId |
| level | level | level |
| level_name | levelName | levelName |

### 分层架构

```
┌─────────────────────┐
│  CampTypeController │  ← Controller 层 (REST API)
├─────────────────────┤
│  CampTypeService    │  ← Service 层 (业务逻辑)
├─────────────────────┤
│ CampTypeMapper      │  ← Mapper 层 (数据访问)
├─────────────────────┤
│   t_camp_type       │  ← 数据库表
└─────────────────────┘
```

---

## 使用示例 (cURL)

### 查询列表
```bash
curl -X GET http://localhost:8080/api/admin/camp-types
```

### 新增分类
```bash
curl -X POST http://localhost:8080/api/admin/camp-types \
  -H "Content-Type: application/json" \
  -d '{"level":"NL","levelName":"努力班"}'
```

### 修改分类
```bash
curl -X PUT http://localhost:8080/api/admin/camp-types \
  -H "Content-Type: application/json" \
  -d '{"typeId":1,"level":"ML","levelName":"明理班"}'
```

### 删除分类
```bash
curl -X DELETE http://localhost:8080/api/admin/camp-types/1
```

---

## 注意事项

1. **鉴权要求**: 所有接口均以 `/api/admin` 开头，需要管理员权限认证
2. **数据一致性**: 删除操作前请确保该分类未被其他表引用
3. **字段约束**: `level` 和 `levelName` 均为必填字段，不能为空字符串
4. **自动生成的 ID**: 新增时 `typeId` 由数据库自增生成，无需手动指定
5. **更新策略**: 更新操作中，`level` 和 `levelName` 为非必填，只更新提供的字段

---

## 相关文件清单

- **Entity**: `CampType.java` - 实体类
- **DTO**: `CampTypeDTO.java` - 数据传输对象
- **Mapper**: `CampTypeMapper.java` - 数据访问接口
- **Mapper XML**: `CampTypeMapper.xml` - SQL 映射文件
- **Service**: `CampTypeService.java` - 服务接口
- **ServiceImpl**: `CampTypeServiceImpl.java` - 服务实现
- **Controller**: `CampTypeController.java` - REST 控制器
