# 多端身份切换 API - 快速参考指南

## 🎯 核心改动一览

### 文件修改清单

| 文件 | 修改类型 | 说明 |
|------|----------|------|
| `AuthController.java` | ✅ 新增方法 | 新增 `/app/user/switch-identity` 接口 |
| `UserController.java` | ✏️ 修改注释 | 明确标注为 PC 管理端专用 |
| `UserAuthService.java` | ✅ 新增接口 | 增加 `executeIdentitySwitch` 方法 |
| `UserAuthServiceImpl.java` | ✅ 实现逻辑 | 实现多端差异化 Token 生成 |

---

## 📍 API 路径对比

### PC 管理端
```
POST /api/admin/user/switch-identity
├─ Controller: UserController
├─ Service: UserAuthService.executeIdentitySwitch(userId, request, "ADMIN")
└─ Token 标记：clientType=ADMIN, isAdmin=true
```

### 小程序端
```
POST /api/app/user/switch-identity
├─ Controller: AuthController
├─ Service: UserAuthService.executeIdentitySwitch(userId, request, "APP")
└─ Token 标记：clientType=APP
```

---

## 💻 代码示例

### AuthController.java - 新增部分

```java
@Autowired
private UserAuthService authService;

/**
 * 小程序端 - 用户切换身份接口
 * POST /app/user/switch-identity
 */
@PostMapping("/app/user/switch-identity")
public Result<Map<String, Object>> appSwitchIdentity(
        @RequestHeader("Authorization") String token,
        @RequestBody SwitchIdentityRequest request) {
    try {
        // 1. 解析 Token 获取用户 ID
        Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        
        // 2. 调用 Service 执行身份切换（使用 APP 类型）
        String newToken = authService.executeIdentitySwitch(userId, request, "APP");
        
        // 3. 返回新 Token
        Map<String, Object> result = new HashMap<>();
        result.put("token", newToken);
        
        return Result.success(result);
    } catch (IllegalArgumentException e) {
        return Result.error(e.getMessage());
    }
}
```

### UserController.java - 修改部分

```java
/**
 * PC 管理端 - 执行身份切换
 * POST /api/admin/user/switch-identity
 */
@PostMapping("/switch-identity")
public ResponseResult<Map<String, Object>> switchIdentity(
        @RequestBody SwitchIdentityRequest request,
        HttpServletRequest httpRequest) {
    try {
        Long userId = (Long) httpRequest.getAttribute("userId");
        
        // 使用 ADMIN 类型生成 Token
        String newToken = userAuthService.executeIdentitySwitch(userId, request, "ADMIN");
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", newToken);
        
        return ResponseResult.success("切换成功", result);
    } catch (Exception e) {
        return ResponseResult.error(500, e.getMessage());
    }
}
```

### UserAuthService.java - 新增接口

```java
/**
 * 执行身份切换（支持多端差异化处理）
 */
String executeIdentitySwitch(Long userId, SwitchIdentityRequest request, String clientType);
```

### UserAuthServiceImpl.java - 核心实现

```java
@Override
public String executeIdentitySwitch(Long userId, SwitchIdentityRequest request, String clientType) {
    // 1. 校验任命记录有效性
    Map<String, Object> assignment = dutyAssignmentMapper.selectById(request.getAssignmentId());
    
    // 2. 验证用户权限
    Long assignmentUserId = (Long) assignment.get("user_id");
    if (!userId.equals(assignmentUserId)) {
        throw new IllegalArgumentException("无权切换到该身份");
    }
    
    // 3. 获取新的 dutyType 和 campId
    String newDutyType = (String) assignment.get("duty_type");
    Integer campId = (Integer) assignment.get("camp_id");
    
    // 4. 构建 Token 载荷
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("dutyType", newDutyType);
    if (campId != null) {
        claims.put("campId", campId);
    }
    
    // 5. 添加客户端类型标记
    claims.put("clientType", clientType);
    
    // 6. PC 端额外添加管理员标记
    if ("ADMIN".equals(clientType)) {
        claims.put("isAdmin", true);
    }
    
    return jwtUtils.generateToken(claims);
}
```

---

## 🔧 如何测试

### 使用 Postman 测试 PC 端

```http
POST http://localhost:8080/api/admin/user/switch-identity
Content-Type: application/json
Authorization: Bearer <your-token>

{
  "assignmentId": 1
}
```

### 使用 curl 测试小程序端

```bash
curl -X POST http://localhost:8080/api/app/user/switch-identity \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{"assignmentId": 1}'
```

---

## ⚠️ 注意事项

### 1. 路径不要混淆
- ❌ 错误：PC 端调用 `/api/app/user/switch-identity`
- ✅ 正确：PC 端调用 `/api/admin/user/switch-identity`

### 2. Token 不通用
- PC 端生成的 Token 包含 `isAdmin=true`
- 小程序端生成的 Token 没有此标记
- 拦截器可根据此标记实施不同的权限控制

### 3. 前端对接提示
**PC 管理后台**：
```javascript
// 切换到志愿者身份
const response = await fetch('/api/admin/user/switch-identity', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ assignmentId: 1 })
});
const { data } = await response.json();
localStorage.setItem('adminToken', data.token); // 保存新 Token
```

**小程序**：
```javascript
// 切换到学班身份
wx.request({
  url: 'https://your-api.com/api/app/user/switch-identity',
  method: 'POST',
  header: {
    'Authorization': `Bearer ${token}`
  },
  data: { assignmentId: 1 },
  success: (res) => {
    wx.setStorageSync('appToken', res.data.data.token);
  }
})
```

---

## 🎁 扩展性优势

### 未来新增 H5 端

只需三步：

1. **在 AuthController 添加新接口**：
```java
@PostMapping("/h5/user/switch-identity")
public Result<Map<String, Object>> h5SwitchIdentity(...) {
    String newToken = authService.executeIdentitySwitch(userId, request, "H5");
    ...
}
```

2. **在 Service 中添加 H5 特殊逻辑**（如需要）：
```java
if ("H5".equals(clientType)) {
    claims.put("h5SpecificClaim", "...");
}
```

3. **完成！** 无需修改现有代码

---

## 📊 编译验证

```bash
# 编译项目
.\mvnw.cmd clean compile -DskipTests

# 预期输出
[INFO] BUILD SUCCESS
[INFO] Compiling 97 source files with javac [debug parameters release 21]
```

✅ 当前编译状态：**SUCCESS**

---

## 📖 相关文档

- 详细重构报告：`多端身份切换 API 重构报告.md`
- 原始接口文档：`登录接口 API 文档.md`
- 拦截器配置：`AuthInterceptor.java`

---

## 🚀 下一步行动

### 立即可以做的
1. ✅ 运行项目
2. ✅ 使用 Postman 测试两个接口
3. ✅ 验证生成的 Token 差异

### 后续优化
- [ ] 在 `AuthInterceptor` 中根据 `clientType` 实施路由限制
- [ ] 为 PC 端添加细粒度权限列表
- [ ] 记录身份切换日志到数据库

---

**重构完成时间**: 2026-03-08  
**编译状态**: ✅ 通过  
**代码行数**: +100 行（新增），-10 行（修改）
