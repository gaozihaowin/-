# 营期管理新增与编辑 API文档

## 接口概览

为教务系统的核心表 `t_camp`（营期表）提供新增和编辑功能的 RESTful 接口。

---

## 1. 新增营期

### 接口信息
- **请求路径**: `POST /api/admin/camps`
- **请求方法**: POST
- **Content-Type**: `application/json`

### 请求参数
**请求体 JSON 示例**:
```json
{
  "typeId": 1,
  "term": 71,
  "name": "【致良知线上课堂】诚意班",
  "intro": "让内心充满力量的生命哲学课",
  "startTime": "2026-10-01 00:00:00",
  "endTime": "2026-12-31 00:00:00",
  "status": 0,
  "tag": "热招"
}
```

**字段说明**:
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| typeId | Integer | 是 | 营期类型 ID，关联 t_camp_type 表 |
| term | Integer | 是 | 期数，如：71 表示第 71 期 |
| name | String | 是 | 营期名称 |
| intro | String | 否 | 营期介绍 |
| startTime | String | 是 | 开营时间，格式：`yyyy-MM-dd HH:mm:ss` |
| endTime | String | 是 | 结营时间，格式：`yyyy-MM-dd HH:mm:ss` |
| status | Integer | 否 | 状态：0-未开始，1-进行中，2-已结束（默认 0） |
| tag | String | 否 | 标签，如："热招"、"新品"等 |

**重要说明**:
- `enroll_count` 字段由后端强制设为 0，不接受前端传值
- 日期时间格式必须为 `yyyy-MM-dd HH:mm:ss`

### 响应结果
**成功响应**:
```json
{
  "code": 200,
  "msg": "新增成功",
  "data": null,
  "timestamp": 1709884800000
}
```

---

## 2. 编辑营期

### 接口信息
- **请求路径**: `PUT /api/admin/camps`
- **请求方法**: PUT
- **Content-Type**: `application/json`

### 请求参数
**请求体 JSON 示例**:
```json
{
  "campId": 101,
  "typeId": 1,
  "term": 71,
  "name": "【致良知线上课堂】诚意班",
  "intro": "让内心充满力量的生命哲学课",
  "startTime": "2026-10-01 00:00:00",
  "endTime": "2026-12-31 00:00:00",
  "status": 1,
  "tag": "热招"
}
```

**字段说明**:
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| campId | Integer | 是 | 营期 ID，主键（编辑时必填） |
| typeId | Integer | 是 | 营期类型 ID |
| term | Integer | 是 | 期数 |
| name | String | 是 | 营期名称 |
| intro | String | 否 | 营期介绍 |
| startTime | String | 是 | 开营时间，格式：`yyyy-MM-dd HH:mm:ss` |
| endTime | String | 是 | 结营时间，格式：`yyyy-MM-dd HH:mm:ss` |
| status | Integer | 否 | 状态：0-未开始，1-进行中，2-已结束 |
| tag | String | 否 | 标签 |

**重要说明**:
- **必须包含 `campId` 字段**，否则会抛出异常："编辑营期时，campId 不能为空"
- **不会更新 `enroll_count` 字段**，保留真实的报名人数，防止覆盖

### 响应结果
**成功响应**:
```json
{
  "code": 200,
  "msg": "修改成功",
  "data": null,
  "timestamp": 1709884800000
}
```

---

## 业务逻辑说明

### 新增营期业务流程
1. 接收前端传来的 `CampDTO` 对象
2. 将 DTO 转换为 `Camp` 实体
3. **强制设置 `enrollCount = 0`**（不接受前端传值）
4. 调用 Mapper 执行 `INSERT INTO t_camp`
5. 返回成功响应

### 编辑营期业务流程
1. 接收前端传来的 `CampDTO` 对象
2. **校验 `campId` 是否为空**，为空则抛出异常
3. 将 DTO 转换为 `Camp` 实体
4. **不更新 `enrollCount` 字段**（保留真实报名人数）
5. 调用 Mapper 执行 `UPDATE t_camp WHERE camp_id = #{campId}`
6. 返回成功响应

---

## 数据库表结构

### t_camp 表
| 字段名 | 类型 | 说明 |
|--------|------|------|
| camp_id | INT (PK) | 营期 ID，自增主键 |
| type_id | INT | 营期类型 ID，外键关联 t_camp_type |
| term | INT | 期数 |
| name | VARCHAR | 营期名称 |
| intro | TEXT | 营期介绍 |
| start_time | DATETIME | 开营时间 |
| end_time | DATETIME | 结营时间 |
| status | TINYINT | 状态：0-未开始，1-进行中，2-已结束 |
| tag | VARCHAR | 标签 |
| enroll_count | INT | 报名人数 |

---

## 代码实现

### DTO 类：CampDTO.java
```java
package com.daily.dailychineseculture.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 营期管理 DTO
 * 用于新增和编辑营期信息
 */
@Data
public class CampDTO {
    /**
     * 营期 ID（编辑时必填，新增时不填）
     */
    private Integer campId;
    
    /**
     * 营期类型 ID
     */
    private Integer typeId;
    
    /**
     * 期数
     */
    private Integer term;
    
    /**
     * 营期名称
     */
    private String name;
    
    /**
     * 营期介绍
     */
    private String intro;
    
    /**
     * 开营时间
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    
    /**
     * 结营时间
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    
    /**
     * 状态：0 未开始，1 进行中，2 已结束
     */
    private Integer status;
    
    /**
     * 标签
     */
    private String tag;
}
```

**关键点**:
- 使用 `@JsonFormat` 注解确保日期格式正确映射
- 时区设置为 `GMT+8`（中国标准时间）

### Controller 层：CampController.java
```java
@RestController
@RequestMapping("/api/admin/camps")
@RequiredArgsConstructor
public class CampController {
    
    private final CampService campService;
    
    /**
     * 新增营期
     * POST /api/admin/camps
     */
    @PostMapping
    public ResponseResult<String> addCamp(@RequestBody CampDTO campDTO) {
        campService.addCamp(campDTO);
        return ResponseResult.success("新增成功");
    }
    
    /**
     * 编辑营期
     * PUT /api/admin/camps
     */
    @PutMapping
    public ResponseResult<String> updateCamp(@RequestBody CampDTO campDTO) {
        campService.updateCamp(campDTO);
        return ResponseResult.success("修改成功");
    }
}
```

### Service 层：CampServiceImpl.java
```java
@Service
public class CampServiceImpl implements CampService {
    
    @Autowired
    private CampMapper campMapper;
    
    @Override
    public void addCamp(CampDTO campDTO) {
        Camp camp = new Camp();
        // 属性拷贝...
        // 强制设置 enroll_count 为 0
        camp.setEnrollCount(0);
        campMapper.insertCamp(camp);
    }
    
    @Override
    public void updateCamp(CampDTO campDTO) {
        if (campDTO.getCampId() == null) {
            throw new IllegalArgumentException("编辑营期时，campId 不能为空");
        }
        Camp camp = new Camp();
        // 属性拷贝...
        // 注意：不要更新 enroll_count
        campMapper.updateCamp(camp);
    }
}
```

### Mapper 层：CampMapper.xml
```xml
<!-- 新增营期 -->
<insert id="insertCamp" parameterType="com.daily.dailychineseculture.entity.Camp" useGeneratedKeys="true" keyProperty="campId">
    INSERT INTO t_camp (
        type_id, term, name, intro, start_time, end_time, status, tag, enroll_count
    ) VALUES (
        #{typeId}, #{term}, #{name}, #{intro}, #{startTime}, #{endTime}, #{status}, #{tag}, #{enrollCount}
    )
</insert>

<!-- 编辑营期 -->
<update id="updateCamp" parameterType="com.daily.dailychineseculture.entity.Camp">
    UPDATE t_camp
    SET
        type_id = #{typeId},
        term = #{term},
        name = #{name},
        intro = #{intro},
        start_time = #{startTime},
        end_time = #{endTime},
        status = #{status},
        tag = #{tag}
    WHERE camp_id = #{campId}
</update>
```

---

## 测试用例

### 测试 1：新增营期
```bash
curl -X POST http://localhost:8080/api/admin/camps \
  -H "Content-Type: application/json" \
  -d '{
    "typeId": 1,
    "term": 71,
    "name": "【致良知线上课堂】诚意班",
    "intro": "让内心充满力量的生命哲学课",
    "startTime": "2026-10-01 00:00:00",
    "endTime": "2026-12-31 00:00:00",
    "status": 0,
    "tag": "热招"
  }'
```

### 测试 2：编辑营期
```bash
curl -X PUT http://localhost:8080/api/admin/camps \
  -H "Content-Type: application/json" \
  -d '{
    "campId": 101,
    "typeId": 1,
    "term": 71,
    "name": "【致良知线上课堂】诚意班",
    "intro": "让内心充满力量的生命哲学课",
    "startTime": "2026-10-01 00:00:00",
    "endTime": "2026-12-31 00:00:00",
    "status": 1,
    "tag": "热招"
  }'
```

### 测试 3：编辑营期 - 缺少 campId（应失败）
```bash
curl -X PUT http://localhost:8080/api/admin/camps \
  -H "Content-Type: application/json" \
  -d '{
    "typeId": 1,
    "term": 71,
    "name": "【致良知线上课堂】诚意班"
  }'
```

预期响应：
```json
{
  "code": 500,
  "msg": "编辑营期时，campId 不能为空",
  "data": null
}
```

---

## 注意事项

### ⚠️ 高危防错提醒

1. **日期格式处理**
   - 前端可能传标准的 `yyyy-MM-dd HH:mm:ss` 字符串
   - DTO 中已添加 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")` 注解
   - 确保 MySQL 的 DATETIME 类型能被正确映射

2. **enroll_count 字段保护**
   - **新增时**：强制设为 0，不接受前端传值
   - **编辑时**：绝对不要更新该字段，防止覆盖真实的报名人数

3. **campId 校验**
   - 编辑接口必须校验 campId 是否存在
   - 为空时立即抛出异常，避免误操作

---

## 相关文件列表

- **DTO**: `/src/main/java/com/daily/dailychineseculture/dto/CampDTO.java`
- **Entity**: `/src/main/java/com/daily/dailychineseculture/entity/Camp.java`
- **Controller**: `/src/main/java/com/daily/dailychineseculture/controller/CampController.java`
- **Service**: `/src/main/java/com/daily/dailychineseculture/service/CampService.java`
- **ServiceImpl**: `/src/main/java/com/daily/dailychineseculture/service/impl/CampServiceImpl.java`
- **Mapper**: `/src/main/java/com/daily/dailychineseculture/mapper/CampMapper.java`
- **Mapper XML**: `/src/main/resources/mapper/CampMapper.xml`

---

## 版本历史

| 版本 | 日期 | 修改内容 | 作者 |
|------|------|----------|------|
| v1.0 | 2026-03-08 | 初始版本，新增营期 CRUD 接口 | AI Assistant |
