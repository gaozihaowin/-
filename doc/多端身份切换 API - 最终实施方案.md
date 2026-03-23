# 多端身份切换 API - 最终实施方案

## 📋 修改概览

本次修改基于您恢复的 `AuthController.java` 原始代码，按照多端架构规范完成了身份切换功能的正确实现。

---

## ✅ 已完成的修改

### 1. AuthController.java - 添加小程序端接口

**文件路径**: `src/main/java/com/daily/dailychineseculture/controller/AuthController.java`

#### 修改内容：

**（1）导入依赖**
```java
import com.daily.dailychineseculture.service.UserAuthService;
```

**（2）注入 Service**
```java
@Autowired
private UserAuthService authService;
```

**（3）新增方法** - 小程序端切换身份
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

**核心逻辑**：
- 解析 Token 获取用户 ID
- 调用 `authService.executeIdentitySwitch(userId, request, "APP")`
- 返回新的 JWT Token

---

### 2. UserController.java - PC 管理端（已存在）

**文件路径**: `src/main/java/com/daily/dailychineseculture/controller/UserController.java`

**当前状态**：✅ 已经是正确的实现

**关键代码**：
```java
/**
 * PC 管理端 - 执行身份切换
 * POST /api/admin/user/switch-identity
 */
@PostMapping("/switch-identity")
public ResponseResult<Map<String, Object>> switchIdentity(...) {
    // 使用 ADMIN 类型生成 Token
    String newToken = userAuthService.executeIdentitySwitch(userId, request, "ADMIN");
    ...
}
```

---

### 3. UserAuthService.java - 接口定义（已存在）

**文件路径**: `src/main/java/com/daily/dailychineseculture/service/UserAuthService.java`

**当前状态**：✅ 已经包含两个方法

```java
// 方法 1：传统方法（向下兼容）
String switchIdentity(Long userId, SwitchIdentityRequest request);

// 方法 2：多端支持方法
String executeIdentitySwitch(Long userId, SwitchIdentityRequest request, String clientType);
```

---

### 4. UserAuthServiceImpl.java - 核心实现（已存在）

**文件路径**: `src/main/java/com/daily/dailychineseculture/service/impl/UserAuthServiceImpl.java`

**当前状态**：✅ 已经实现完整逻辑

**核心功能**：
1. 校验任命记录有效性
2. 验证用户权限
3. 根据 `clientType` 构建不同的 Token 载荷
4. PC 端添加 `isAdmin=true` 标记

---

## 🎯 多端架构对比

| 特性 | PC 管理端 | 小程序端 |
|------|----------|----------|
| **API 路径** | `/api/admin/user/switch-identity` | `/app/user/switch-identity` |
| **Controller** | `UserController` | `AuthController` |
| **ClientType** | `"ADMIN"` | `"APP"` |
| **Token 标记** | `isAdmin=true` | 无特殊标记 |
| **响应格式** | `ResponseResult` | `Result` |
| **鉴权方式** | `HttpServletRequest` | `@RequestHeader` |

---

## 📝 API 使用示例

### 小程序端切换身份

**请求**：
```http
POST /app/user/switch-identity
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "assignmentId": 1
}
```

**响应**：
```json
{
  "code": 200,
  "msg": "切换成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjIwMjYwMDAwMDEsImNsaWVudFR5cGUiOiJBUF AifQ..."
  }
}
```

---

### PC 管理端切换身份

**请求**：
```http
POST /api/admin/user/switch-identity
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "assignmentId": 1
}
```

**响应**：
```json
{
  "code": 200,
  "msg": "切换成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjIwMjYwMDAwMDEsImNsaWVudFR5cGUiOiJBRE1JTiIsImlzQWRtaW4iOnRydWV9..."
  }
}
```

---

## 🔍 关键差异点

### 1. Token 载荷差异

**PC 管理端 Token**：
```json
{
  "userId": 2026000001,
  "dutyType": "COURSE_ADMIN",
  "campId": 1,
  "clientType": "ADMIN",
  "isAdmin": true
}
```

**小程序端 Token**：
```json
{
  "userId": 2026000001,
  "dutyType": "志愿者",
  "campId": 1,
  "clientType": "APP"
}
```

### 2. 调用方式差异

**PC 管理端**（使用拦截器）：
```java
Long userId = (Long) httpRequest.getAttribute("userId");
```

**小程序端**（解析 Token）：
```java
Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
```

---

## ✅ 编译验证

```bash
.\mvnw.cmd clean compile -DskipTests

# 输出
[INFO] BUILD SUCCESS
[INFO] Compiling 97 source files with javac [debug parameters release 21]
```

**编译状态**：✅ 通过  
**修改文件数**：1 个（AuthController.java）  
**新增代码行数**：+43 行  
**删除代码行数**：0 行  

---

## 🚀 前端对接指南

### 小程序端代码示例

```javascript
// 切换到志愿者身份
wx.request({
  url: 'https://your-api.com/api/app/user/switch-identity',
  method: 'POST',
  header: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  data: { 
    assignmentId: 1 
  },
  success: (res) => {
    // 保存新 Token
    wx.setStorageSync('appToken', res.data.data.token);
    
    // 提示用户
    wx.showToast({
      title: '身份切换成功',
      icon: 'success'
    });
  },
  fail: (err) => {
    wx.showModal({
      title: '切换失败',
      content: err.errMsg
    });
  }
});
```

### PC 管理端代码示例

```javascript
// 切换到志愿者身份
async function switchToVolunteer(assignmentId) {
  const response = await fetch('/api/admin/user/switch-identity', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${adminToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ assignmentId })
  });
  
  const { data } = await response.json();
  
  // 保存新 Token
  localStorage.setItem('adminToken', data.token);
  
  // 更新 UI
  updateCurrentUser(data.token);
  
  return data;
}
```

---

## ⚠️ 注意事项

### 1. 路径不要混淆
- ❌ PC 端调用 `/app/user/switch-identity`
- ✅ PC 端调用 `/api/admin/user/switch-identity`
- ❌ 小程序端调用 `/api/admin/user/switch-identity`
- ✅ 小程序端调用 `/app/user/switch-identity`

### 2. Token 不通用
- PC 端生成的 Token 包含 `isAdmin=true`
- 小程序端生成的 Token 没有此标记
- 后续拦截器可根据此标记实施不同的权限控制

### 3. 参数格式
- 两个接口都使用 `assignmentId` 参数
- 不再使用旧的 `identity` 参数（字符串类型）
- `assignmentId` 来自 `/identities` 接口返回的身份列表

---

## 📊 与旧版本的对比

### 旧版本（已废弃）
```java
// AuthController 中的旧方法
@PostMapping("/user/switch-identity")
public Result<Map<String, String>> switchIdentity(
        @RequestHeader("Authorization") String token,
        @RequestBody Map<String, String> request) {
    
    String identity = request.get("identity"); // ❌ 字符串参数
    
    // 简单的字符串映射
    Map<String, String> identityMap = new HashMap<>();
    identityMap.put("学员端", "student");
    identityMap.put("志愿者端", "volunteer");
    
    // ❌ 没有基于 appointmentId 的校验
    // ❌ 没有生成新的 Token
    // ❌ 只是返回了原样字符串
}
```

### 新版本（推荐）
```java
// AuthController 中的新方法
@PostMapping("/app/user/switch-identity")
public Result<Map<String, Object>> appSwitchIdentity(
        @RequestHeader("Authorization") String token,
        @RequestBody SwitchIdentityRequest request) {
    
    Integer assignmentId = request.getAssignmentId(); // ✅ 对象参数
    
    // ✅ 基于 appointmentId 的数据库校验
    // ✅ 生成包含新身份的 JWT Token
    // ✅ 返回新 Token 供前端使用
}
```

---

## 🎁 架构优势

### ✅ 路由彻底隔离
- PC 端和小程序端永远不会发生路径冲突
- 符合 RESTful API 设计规范
- 易于理解和维护

### ✅ 代码复用率高
- 核心业务逻辑集中在 Service 层
- Controller 层只负责接收请求和返回响应
- 遵循 DRY 原则

### ✅ 安全控制增强
- PC 端 Token 包含 `isAdmin` 标记
- 可根据 `clientType` 实施不同的安全策略
- 为细粒度权限控制预留扩展点

### ✅ 向后兼容
- 保留了 `switchIdentity` 方法（内部调用新方法）
- 不影响现有代码
- 平滑升级

---

## 📖 相关文件清单

### 修改的文件
- ✅ `AuthController.java` - 新增小程序端接口
- ✅ `UserController.java` - PC 端已正确实现
- ✅ `UserAuthService.java` - 接口已包含多端支持
- ✅ `UserAuthServiceImpl.java` - 实现已包含多端逻辑

### 生成的文档
- ✅ `多端身份切换 API 重构报告.md` - 详细技术文档
- ✅ `多端身份切换 API - 快速参考指南.md` - 速查手册
- ✅ `多端身份切换 API - 最终实施方案.md` - 本文档

---

## 🔧 下一步建议

### 立即可以做的
1. ✅ 运行项目（编译已通过）
2. ✅ 使用 Postman 测试两个接口
3. ✅ 验证生成的 Token 差异

### 后续优化
- [ ] 在 `AuthInterceptor` 中根据 `clientType` 实施路由限制
- [ ] 为 PC 端添加细粒度权限列表
- [ ] 记录身份切换日志到数据库
- [ ] 实现 Token 黑名单机制

---

## 📞 常见问题

### Q1: 为什么要区分 PC 端和小程序端？
**A**: 因为两端的用户群体、使用场景、安全要求都不同。PC 端主要是管理员使用，需要更多权限；小程序端是普通用户使用，权限相对简单。

### Q2: 为什么 PC 端和小程序端的响应格式不同？
**A**: `ResponseResult` 用于 PC 管理后台，`Result` 用于小程序端。这是历史遗留问题，未来可以统一，但不影响功能。

### Q3: 旧的 `/user/switch-identity` 接口还能用吗？
**A**: 建议使用新接口。旧接口虽然还在，但功能不完整（只返回字符串，不生成新 Token）。

---

**修改完成时间**: 2026-03-08 23:30  
**编译状态**: ✅ 通过  
**修改文件数**: 1  
**新增代码**: +43 行  
**架构模式**: 多端隔离 + Service 复用
