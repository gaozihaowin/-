# PC 端后台管理多角色登录鉴权 API

## 一、功能概述

为 PC 端后台管理系统实现完整的多角色登录鉴权体系，包括：
1. **管理员登录接口**：支持多种角色身份登录（课程管理、档案管理、志愿者等）
2. **全局 Token 拦截器**：自动验证 Token 有效性并注入用户信息到请求上下文

---

## 二、数据库结构

### 1. 用户表 `t_user`
```sql
CREATE TABLE t_user (
    user_id BIGINT PRIMARY KEY,
    account VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    -- 其他字段...
);
```

### 2. 职位分配表 `t_duty_assignment`
```sql
CREATE TABLE t_duty_assignment (
    assignment_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    camp_id INT,  -- 营期 ID（全局管理员为 null）
    duty_type VARCHAR(50) NOT NULL,  -- 职位类型代码：COURSE_ADMIN, ARCHIVE_ADMIN, VOLUNTEER 等
    duty_name VARCHAR(100),  -- 职位名称
    status INT DEFAULT 1,  -- 状态：1 正常，0 冻结
    start_time DATETIME,  -- 任职开始时间
    end_time DATETIME,  -- 任职结束时间（null 表示永久）
    volunteer_start_time DATETIME,  -- 志愿者服务开始时间
    volunteer_end_time DATETIME,  -- 志愿者服务结束时间
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

---

## 三、API 接口设计

### 1. 管理员登录接口

#### 接口路径
```
POST /api/admin/login
```

#### 请求参数（JSON Body）
```json
{
  "account": "student02",
  "password": "123",
  "loginRole": "COURSE_ADMIN"
}
```

**参数说明：**
- `account`: 账号（必填）
- `password`: 密码（必填，明文传输，建议生产环境使用 HTTPS+ 加密）
- `loginRole`: 登录角色类型（必填）
  - `COURSE_ADMIN`: 课程管理
  - `ARCHIVE_ADMIN`: 档案管理
  - `VOLUNTEER`: 志愿者

#### 成功响应（HTTP 200）
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "userId": "2026000002",
      "account": "student02",
      "nickname": "王老师",
      "currentRole": "COURSE_ADMIN",
      "campId": null
    }
  }
}
```

#### 错误响应示例

**密码错误（HTTP 200）**
```json
{
  "code": 401,
  "msg": "账号或密码错误",
  "data": null
}
```

**无权限（HTTP 200）**
```json
{
  "code": 403,
  "msg": "抱歉，您当前账号无权以该身份登录",
  "data": null
}
```

**账号被冻结（HTTP 200）**
```json
{
  "code": 400,
  "msg": "您的账号已被冻结，请联系管理员",
  "data": null
}
```

---

### 2. 需要鉴权的管理接口示例

#### 接口路径
```
GET /api/admin/current
```

#### 请求头
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 成功响应
```
后端控制台输出：
=== 当前管理员信息 ===
userId: 2026000002
currentRole: COURSE_ADMIN
campId: null
```

#### 错误响应

**无 Token（HTTP 401）**
```json
{
  "code": 401,
  "msg": "Token 已过期或未登录"
}
```

**Token 过期（HTTP 401）**
```json
{
  "code": 401,
  "msg": "Token 已过期或无效"
}
```

---

## 四、核心业务逻辑

### 1. 登录流程

```
1. 接收登录请求（account, password, loginRole）
   ↓
2. 参数校验（空值检查）
   ↓
3. 根据账号查询用户
   ├─ 用户不存在 → 返回"账号或密码错误"
   └─ 用户存在 → 继续
   ↓
4. 校验密码
   ├─ 密码错误 → 返回"账号或密码错误"
   └─ 密码正确 → 继续
   ↓
5. 查询职位权限（联查 t_duty_assignment）
   SQL: SELECT * FROM t_duty_assignment 
        WHERE user_id = ? 
        AND duty_type = ? 
        AND status = 1 
        AND (end_time IS NULL OR end_time > NOW())
   ├─ 查不到记录 → 返回"无权以该身份登录"
   └─ 查到记录 → 继续
   ↓
6. 检查状态
   ├─ status != 1 → 返回"账号已被冻结"
   └─ status == 1 → 继续
   ↓
7. 生成 JWT Token
   Payload 包含：userId, currentRole, campId
   ↓
8. 返回 Token 和用户信息
```

### 2. Token 拦截器流程

```
1. 拦截所有 /api/admin/** 请求
   ↓
2. 放行 /api/admin/login
   ↓
3. 从 Authorization 头提取 Token
   ├─ 无 Token → 返回 401
   └─ 有 Token → 继续
   ↓
4. 去除 "Bearer " 前缀
   ↓
5. 验证 Token 有效性
   ├─ 格式错误/过期 → 返回 401
   └─ 有效 → 继续
   ↓
6. 解析 Token 获取用户信息
   - userId
   - currentRole
   - campId
   ↓
7. 存入 request attribute
   - request.setAttribute("userId", userId)
   - request.setAttribute("currentRole", currentRole)
   - request.setAttribute("campId", campId)
   ↓
8. 放行请求
```

---

## 五、文件清单

### 1. Entity 实体类
- `DutyAssignment.java` - 职位分配实体

### 2. DTO 数据传输对象
- `AdminLoginRequest.java` - 管理员登录请求 DTO
- `AdminLoginResult.java` - 管理员登录响应 DTO

### 3. Mapper 数据访问层
- `DutyAssignmentMapper.java` - 职位分配 Mapper

### 4. Service 业务逻辑层
- `AdminAuthService.java` - 管理员认证服务接口
- `AdminAuthServiceImpl.java` - 管理员认证服务实现

### 5. Controller 控制器层
- `AdminController.java` - 管理员登录控制器
- `AdminTestController.java` - 测试控制器（演示获取用户信息）

### 6. Interceptor 拦截器
- `AdminAuthInterceptor.java` - PC 端后台管理鉴权拦截器

### 7. Config 配置类
- `WebConfig.java` - Web MVC 配置（已更新，注册拦截器）

### 8. Util 工具类
- `JwtUtils.java` - JWT 工具类（已更新，支持多角色）

### 9. Test 测试类
- `AdminAuthApiTest.java` - API 接口测试

---

## 六、关键代码片段

### 1. JWT Token 生成（包含多角色信息）
```java
public String generateToken(Long userId, String username, String currentRole, Integer campId) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
    
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("username", username);
    claims.put("currentRole", currentRole);
    if (campId != null) {
        claims.put("campId", campId);
    }
    
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SECRET_KEY)
            .compact();
}
```

### 2. 密码条件更新安全处理
```java
// 查询用户职位权限
Map<String, Object> dutyInfo = dutyAssignmentMapper.selectWithCampInfo(
    user.getUserId(), 
    request.getLoginRole()
);

if (dutyInfo == null) {
    throw new RuntimeException("抱歉，您当前账号无权以该身份登录");
}

// 检查状态
Integer status = (Integer) dutyInfo.get("status");
if (status != null && status != 1) {
    throw new RuntimeException("您的账号已被冻结，请联系管理员");
}
```

### 3. 拦截器注入用户信息
```java
// 将用户信息存入请求上下文
Long userId = jwtUtils.getUserIdFromToken(token);
String currentRole = jwtUtils.getCurrentRoleFromToken(token);
Integer campId = jwtUtils.getCampIdFromToken(token);

request.setAttribute("userId", userId);
request.setAttribute("currentRole", currentRole);
request.setAttribute("campId", campId);
```

### 4. Controller 中获取用户信息
```java
@GetMapping("/current")
public Result getCurrentAdmin(HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    String currentRole = (String) request.getAttribute("currentRole");
    Integer campId = (Integer) request.getAttribute("campId");
    
    // 基于 userId, currentRole, campId 进行数据隔离
    // ...
}
```

---

## 七、前端调用示例

### 1. 登录
```javascript
const loginResponse = await uni.request({
  url: 'http://localhost:8080/api/admin/login',
  method: 'POST',
  header: {
    'Content-Type': 'application/json'
  },
  data: {
    account: 'student02',
    password: '123',
    loginRole: 'COURSE_ADMIN'
  }
});

const { token, userInfo } = loginResponse.data.data;

// 保存 token 到本地存储
uni.setStorageSync('adminToken', token);
uni.setStorageSync('adminUserInfo', userInfo);
```

### 2. 调用需要鉴权的接口
```javascript
const adminToken = uni.getStorageSync('adminToken');

const response = await uni.request({
  url: 'http://localhost:8080/api/admin/current',
  method: 'GET',
  header: {
    'Authorization': `Bearer ${adminToken}`
  }
});
```

### 3. Token 过期处理
```javascript
if (response.statusCode === 401) {
  // Token 过期，跳转到登录页
  uni.removeStorageSync('adminToken');
  uni.redirectTo({
    url: '/pages/admin/login'
  });
}
```

---

## 八、安全建议

### 1. 密码加密
- 当前示例使用明文密码比对
- **生产环境必须使用加密存储**（如 BCryptPasswordEncoder）
- 修改 `AdminAuthServiceImpl.java` 中的密码比对逻辑：
```java
// 示例：使用 BCrypt 加密
@Autowired
private BCryptPasswordEncoder passwordEncoder;

if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
    throw new RuntimeException("账号或密码错误");
}
```

### 2. HTTPS 传输
- 生产环境必须使用 HTTPS 协议
- 防止密码和 Token 在传输过程中被窃取

### 3. Token 有效期
- 当前设置为 7 天（604800000 毫秒）
- 可根据安全要求调整为更短时间（如 24 小时）
- 配合 Refresh Token 机制实现长期登录

### 4. 权限粒度控制
- 当前只校验 `duty_type` 和 `status`
- 可扩展为更细粒度的权限控制（如菜单权限、按钮权限、数据范围权限）

---

## 九、扩展功能建议

### 1. 添加 Refresh Token 机制
- Access Token: 短期有效（如 2 小时）
- Refresh Token: 长期有效（如 30 天）
- Access Token 过期后使用 Refresh Token 刷新

### 2. 添加登录日志
```java
// 登录成功后记录日志
loginLogMapper.insert(userId, request.getLoginRole(), request.getRemoteAddr());
```

### 3. 添加验证码
- 防止暴力破解
- 连续密码错误 5 次后要求输入验证码

### 4. 添加账号锁定机制
- 连续密码错误 N 次后锁定账号 M 分钟
- 防止暴力破解

---

## 十、测试用例

### 1. 正常登录
```
账号：student02
密码：123
角色：COURSE_ADMIN
预期：返回 200，包含有效 Token
```

### 2. 密码错误
```
账号：student02
密码：wrong_password
角色：COURSE_ADMIN
预期：返回 401，提示"账号或密码错误"
```

### 3. 无权限
```
账号：student02
密码：123
角色：ARCHIVE_ADMIN（该用户没有此角色）
预期：返回 403，提示"抱歉，您当前账号无权以该身份登录"
```

### 4. Token 拦截器 - 无 Token
```
接口：GET /api/admin/current
Header: 无 Authorization
预期：返回 401，提示"Token 已过期或未登录"
```

### 5. Token 拦截器 - 有效 Token
```
接口：GET /api/admin/current
Header: Authorization: Bearer <valid_token>
预期：放行请求，控制台输出用户信息
```

---

## 十一、部署注意事项

### 1. 数据库初始化
确保 `t_duty_assignment` 表中有正确的职位分配数据：
```sql
INSERT INTO t_duty_assignment (user_id, camp_id, duty_type, duty_name, status, start_time) 
VALUES 
(2026000002, NULL, 'COURSE_ADMIN', '课程管理员', 1, NOW()),
(2026000003, 101, 'VOLUNTEER', '志愿者', 1, NOW());
```

### 2. JWT 密钥配置
当前使用随机密钥，重启后 Token 会失效。生产环境应从配置文件读取固定密钥：
```java
@Value("${jwt.secret-key}")
private String secretKey;
```

### 3. 跨域配置
如果前后端分离部署，需要在 `WebConfig.java` 中添加 CORS 配置：
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/admin/**")
            .allowedOrigins("http://your-pc-admin-domain.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true);
}
```

---

## 十二、总结

✅ **已完成功能：**
1. 管理员登录接口（支持多角色）
2. JWT Token 生成（包含 userId, currentRole, campId）
3. 全局 Token 拦截器（自动验证 + 注入用户信息）
4. 完整的错误处理（401/403/500）
5. 请求上下文透传（Controller 可直接获取用户信息）

🎯 **核心价值：**
- 统一鉴权体系，避免重复代码
- 灵活的 multi-role 支持
- 安全的 Token 管理机制
- 方便的上下文信息获取

📝 **下一步建议：**
- 添加密码加密（BCrypt）
- 实现 Refresh Token 机制
- 添加登录日志和审计
- 实现细粒度权限控制（菜单/按钮/数据范围）
