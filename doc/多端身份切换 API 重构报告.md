# 多端身份切换 API 重构报告

## 重构背景

在之前的实现中，由于删除了 `AuthController.java` 中的 `switch-identity` 方法来解决路由冲突，导致小程序端失去了身份切换功能。这是一个严重的架构错误，因为我们的系统需要同时服务于**微信小程序端**和**PC 后台管理端**两个客户端。

## 架构设计原则

### 1. 严格区分 API 命名空间 (API Namespacing)

通过路径隔离确保不同端的 API 不会冲突：

| 客户端 | 路径前缀 | 完整路径 | Controller |
|--------|----------|----------|------------|
| **PC 管理端** | `/api/admin` | `POST /api/admin/user/switch-identity` | `UserController` |
| **小程序端** | `/api/app` | `POST /api/app/user/switch-identity` | `AuthController` |

### 2. 核心逻辑下沉到 Service 层 (DRY 原则)

两端虽然路由不同，但底层业务逻辑高度一致：
- 校验 `t_duty_assignment` 表的有效性
- 验证用户是否有权切换到该身份
- 重新生成 JWT Token

这些公共逻辑已提取到 `UserAuthService` 中，避免代码重复。

### 3. Token 载荷差异化处理 (Payload Differences)

根据客户端类型构建不同的 JWT Token：

**PC 管理端 Token (`clientType=ADMIN`)**：
```json
{
  "userId": 2026000001,
  "dutyType": "COURSE_ADMIN",
  "campId": 1,
  "clientType": "ADMIN",
  "isAdmin": true
  // TODO: 可扩展 permissions 数组
}
```

**小程序端 Token (`clientType=APP`)**：
```json
{
  "userId": 2026000001,
  "dutyType": "志愿者",
  "campId": 1,
  "clientType": "APP"
}
```

## 重构内容详情

### 1. AuthController.java - 恢复小程序端切换身份功能

**文件路径**: `src/main/java/com/daily/dailychineseculture/controller/AuthController.java`

**新增方法**:
```java
/**
 * 小程序端 - 用户切换身份接口
 * POST /app/user/switch-identity
 */
@PostMapping("/app/user/switch-identity")
public Result<Map<String, Object>> appSwitchIdentity(
        @RequestHeader("Authorization") String token,
        @RequestBody SwitchIdentityRequest request)
```

**依赖注入**:
```java
@Autowired
private UserAuthService authService;
```

### 2. UserController.java - 明确 PC 管理端定位

**文件路径**: `src/main/java/com/daily/dailychineseculture/controller/UserController.java`

**修改内容**:
- 注释明确标注为 "PC 管理端 - 执行身份切换"
- 调用方式更新为 `userAuthService.executeIdentitySwitch(userId, request, "ADMIN")`
- 保持路径不变：`POST /api/admin/user/switch-identity`

### 3. UserAuthService.java - 增加多端支持方法

**文件路径**: `src/main/java/com/daily/dailychineseculture/service/UserAuthService.java`

**新增接口**:
```java
/**
 * 执行身份切换（支持多端差异化处理）
 * 
 * @param userId 用户 ID
 * @param request 切换请求
 * @param clientType 客户端类型（"ADMIN" = PC 管理端，"APP" = 小程序端）
 * @return 新的 JWT Token
 */
String executeIdentitySwitch(Long userId, SwitchIdentityRequest request, String clientType);
```

### 4. UserAuthServiceImpl.java - 实现多端差异化 Token 生成

**文件路径**: `src/main/java/com/daily/dailychineseculture/service/impl/UserAuthServiceImpl.java`

**核心实现**:
```java
@Override
public String executeIdentitySwitch(Long userId, SwitchIdentityRequest request, String clientType) {
    // 1. 校验 appointmentId 有效性
    Map<String, Object> assignment = dutyAssignmentMapper.selectById(request.getAssignmentId());
    
    // 2. 验证用户权限
    Long assignmentUserId = (Long) assignment.get("user_id");
    if (!userId.equals(assignmentUserId)) {
        throw new IllegalArgumentException("无权切换到该身份");
    }
    
    // 3. 构建 Token 载荷
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("dutyType", newDutyType);
    claims.put("campId", campId);
    claims.put("clientType", clientType);
    
    // 4. PC 端额外添加管理员标记
    if ("ADMIN".equals(clientType)) {
        claims.put("isAdmin", true);
    }
    
    return jwtUtils.generateToken(claims);
}
```

## API 使用示例

### PC 管理端切换身份

**请求**:
```http
POST /api/admin/user/switch-identity
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "assignmentId": 1
}
```

**响应**:
```json
{
  "code": 200,
  "msg": "切换成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjIwMjYwMDAwMDEsImNsaWVudFR5cGUiOiJBRE1JTiIsImlzQWRtaW4iOnRydWV9..."
  }
}
```

### 小程序端切换身份

**请求**:
```http
POST /api/app/user/switch-identity
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "assignmentId": 1
}
```

**响应**:
```json
{
  "code": 200,
  "msg": "切换成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjIwMjYwMDAwMDEsImNsaWVudFR5cGUiOiJBUF AifQ..."
  }
}
```

## 核心优势

### ✅ 路由彻底隔离
- PC 端和小程序端永远不会发生路径冲突
- 符合 RESTful API 设计规范
- 易于理解和维护

### ✅ 代码复用率高
- 核心业务逻辑集中在 Service 层
- 避免重复代码，遵循 DRY 原则
- 未来新增其他客户端（如 H5、App）可直接复用

### ✅ 安全控制增强
- PC 端 Token 包含 `isAdmin` 标记，便于后端权限校验
- 可根据 `clientType` 实施不同的安全策略
- 为细粒度权限控制预留扩展点

### ✅ 可维护性强
- Controller 层职责清晰（仅负责接收请求和返回响应）
- Service 层封装核心逻辑（校验、Token 生成）
- 未来修改只需调整 Service 层

## 后续优化建议

### 1. 完善 Token 解析拦截器

在 `AuthInterceptor` 中根据 `clientType` 和 `isAdmin` 实施不同的权限校验：

```java
// 伪代码示例
if (claims.getBoolean("isAdmin")) {
    // 允许访问 PC 管理端接口
} else {
    // 仅允许访问小程序端接口
}
```

### 2. 细化权限控制

为 PC 管理端添加权限列表：
```java
claims.put("permissions", Arrays.asList(
    "USER_MANAGE",      // 用户管理
    "CAMP_MANAGE",      // 营期管理
    "HOMEWORK_REVIEW"   // 作业审核
));
```

### 3. 日志审计

记录身份切换操作日志，便于追踪用户行为：
```java
System.out.println("用户 " + userId + " 从 " + oldDutyType + 
                   " 切换到 " + newDutyType + "，客户端：" + clientType);
```

## 测试验证建议

### 单元测试
- 测试 `executeIdentitySwitch` 方法的参数校验逻辑
- 测试不同 `clientType` 生成的 Token 差异
- 测试越权切换身份的异常处理

### 集成测试
- PC 端登录后切换到志愿者身份
- 小程序端登录后切换到学班身份
- 验证生成的 Token 能否被正确解析

### 前端对接
- PC 管理后台调用 `/api/admin/user/switch-identity`
- 小程序调用 `/api/app/user/switch-identity`
- 验证新 Token 能否正常访问对应端的接口

## 总结

本次重构严格遵循了**多端架构**的设计规范，通过**路由隔离**和**逻辑复用**，实现了：
1. ✅ PC 管理端和小程序端身份切换功能并存
2. ✅ 核心业务逻辑下沉到 Service 层
3. ✅ 根据客户端类型生成差异化 Token
4. ✅ 代码结构清晰，易于维护和扩展

这为未来系统的多端发展奠定了坚实的基础。
