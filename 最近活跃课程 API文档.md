# 最近活跃课程 API文档

## 接口说明

该接口用于 PC端后台管理系统的仪表盘（Dashboard），展示最近活跃的 5 个课程。

---

## 接口契约

### 基本信息

- **接口路径**: `GET /api/admin/dashboard/recent-camps`
- **请求方式**: GET
- **需要鉴权**: ✅ 是（需要在 Header 中携带 Token）

---

### 请求头

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
  "data": [
    {
      "campId": 102,
      "campName": "【致良知线上课堂】笃行班",
      "instructor": "致良知教研团队",
      "visitCount": 5,
      "statusCode": 0,
      "statusText": "待开课",
      "startTime": "2026-03-01 00:00:00"
    },
    {
      "campId": 101,
      "campName": "【致良知线上课堂】诚意班",
      "instructor": "致良知教研团队",
      "visitCount": 573,
      "statusCode": 1,
      "statusText": "进行中",
      "startTime": "2025-12-20 00:00:00"
    }
  ]
}
```

**错误响应示例:**

```json
{
  "code": 401,
  "msg": "Token 已过期或未登录",
  "data": null
}
```

---

## 字段说明

### 响应数据字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| campId | Integer | 营期 ID |
| campName | String | 营期名称 |
| instructor | String | 讲师（固定值："致良知教研团队"） |
| visitCount | Integer | 访问量（使用报名人数 enroll_count 平替） |
| statusCode | Integer | 状态码：0-待开课，1-进行中，2-已结束 |
| statusText | String | 状态文本：待开课/进行中/已结束 |
| startTime | String | 开营时间（格式：yyyy-MM-dd HH:mm:ss） |

---

## 业务逻辑说明

### SQL 查询逻辑

```sql
SELECT camp_id, name, status, start_time, enroll_count
FROM t_camp
ORDER BY start_time DESC
LIMIT 5
```

**关键点：**
1. 按 `start_time` 倒序排列，确保最新的课程排在前面
2. 使用 `LIMIT 5` 限制返回记录数
3. 查询字段包含：营期 ID、名称、状态、开营时间、报名人数

### 状态字典映射

| statusCode | statusText |
|------------|-----------|
| 0 | 待开课 |
| 1 | 进行中 |
| 2 | 已结束 |

### 数据转换规则

1. **campId** ← `t_camp.camp_id`
2. **campName** ← `t_camp.name`
3. **visitCount** ← `t_camp.enroll_count`（暂用报名人数平替访问量）
4. **statusCode** ← `t_camp.status`
5. **statusText** ← Service 层根据 statusCode 转换
6. **startTime** ← `t_camp.start_time`（格式化为 "yyyy-MM-dd HH:mm:ss"）
7. **instructor** ← 硬编码返回 "致良知教研团队"

---

## 实现代码

### 1. Entity - Camp.java

```java
@Data
@Alias("Camp")
public class Camp {
    private Integer campId;
    private Integer typeId;
    private String name;
    private String intro;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private Integer enrollCount;  // 新增字段
}
```

---

### 2. DTO - RecentCampDTO.java

```java
@Data
public class RecentCampDTO {
    
    @JsonProperty("campId")
    private Integer campId;
    
    @JsonProperty("campName")
    private String campName;
    
    @JsonProperty("instructor")
    private String instructor = "致良知教研团队";
    
    @JsonProperty("visitCount")
    private Integer visitCount;
    
    @JsonProperty("statusCode")
    private Integer statusCode;
    
    @JsonProperty("statusText")
    private String statusText;
    
    @JsonProperty("startTime")
    private String startTime;
}
```

---

### 3. Mapper - CampMapper.java

```java
@Mapper
public interface CampMapper {
    
    /**
     * 查询最近活跃的 5 个营期（用于仪表盘展示）
     * 按开营时间倒序排列，返回最新 5 条记录
     */
    @Select("SELECT camp_id, name, status, start_time, enroll_count " +
            "FROM t_camp " +
            "ORDER BY start_time DESC " +
            "LIMIT 5")
    List<Camp> selectRecentCamps();
}
```

---

### 4. Service - CampService.java

```java
public interface CampService {
    
    /**
     * 获取最近活跃的课程列表（用于仪表盘）
     * 按开营时间倒序取最新 5 条记录，并进行状态字典转换
     * @return 最近活跃课程 DTO 列表
     */
    List<RecentCampDTO> getRecentCamps();
}
```

---

### 5. ServiceImpl - CampServiceImpl.java

```java
@Service
public class CampServiceImpl implements CampService {
    
    @Autowired
    private CampMapper campMapper;
    
    @Override
    public List<RecentCampDTO> getRecentCamps() {
        // 查询最近活跃的 5 个营期
        List<Camp> camps = campMapper.selectRecentCamps();
        
        // 转换为 DTO 并处理状态文本
        List<RecentCampDTO> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (Camp camp : camps) {
            RecentCampDTO dto = new RecentCampDTO();
            dto.setCampId(camp.getCampId());
            dto.setCampName(camp.getName());
            dto.setVisitCount(camp.getEnrollCount() != null ? camp.getEnrollCount() : 0);
            dto.setStatusCode(camp.getStatus() != null ? camp.getStatus() : 0);
            dto.setStatusText(getStatusText(camp.getStatus()));
            dto.setStartTime(camp.getStartTime() != null ? sdf.format(camp.getStartTime()) : "");
            
            result.add(dto);
        }
        
        return result;
    }
    
    /**
     * 根据状态码获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "待开课";
        }
        switch (status) {
            case 0:
                return "待开课";
            case 1:
                return "进行中";
            case 2:
                return "已结束";
            default:
                return "未知状态";
        }
    }
}
```

---

### 6. Controller - AdminController.java

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private CampService campService;
    
    /**
     * 获取最近活跃课程列表（仪表盘用）
     * GET /api/admin/dashboard/recent-camps
     * 
     * @return 最近活跃课程列表
     */
    @GetMapping("/dashboard/recent-camps")
    public Result<List<RecentCampDTO>> getRecentCamps() {
        try {
            List<RecentCampDTO> recentCamps = campService.getRecentCamps();
            return Result.success(recentCamps);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取最新课程失败：" + e.getMessage());
        }
    }
}
```

---

## 权限配置

该接口已自动纳入 `AdminAuthInterceptor` 的鉴权范围：

- ✅ 需要携带有效的 JWT Token
- ✅ Token 格式：`Authorization: Bearer <token>`
- ❌ 未授权访问将返回 401 错误

**拦截器配置位置：** `AdminAuthInterceptor.java`

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    // 放行登录接口
    String requestUri = request.getRequestURI();
    if ("/api/admin/login".equals(requestUri)) {
        return true;
    }
    
    // 其他 /api/admin/** 接口都需要验证 Token
    // ...
}
```

---

## 测试用例

### 前置条件

1. 已成功登录 PC端后台管理系统
2. 拥有有效的 JWT Token

### 测试步骤

1. 打开 Postman 或类似工具
2. 创建 GET 请求：`http://localhost:8080/api/admin/dashboard/recent-camps`
3. 添加请求头：`Authorization: Bearer <your_token_here>`
4. 发送请求

### 预期结果

- 状态码：200
- 返回最多 5 条课程记录
- 按开营时间倒序排列
- 每条记录包含完整的字段信息

---

## 数据库表结构

### t_camp 表

```sql
CREATE TABLE t_camp (
    camp_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '营期 ID',
    type_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    intro TEXT,
    start_time DATETIME,
    end_time DATETIME,
    status TINYINT DEFAULT 0,
    enroll_count INT DEFAULT 0 COMMENT '报名人数',
    FOREIGN KEY (type_id) REFERENCES t_camp_type(type_id)
) COMMENT = '营期表';
```

**注意：** 如果数据库中还没有 `enroll_count` 字段，需要执行以下 SQL 添加：

```sql
ALTER TABLE t_camp ADD COLUMN enroll_count INT DEFAULT 0 COMMENT '报名人数';
```

---

## 开发完成时间

2026-03-07
