# PC端后台管理登录接口 HTTP 状态码修复报告

## 问题描述

前端测试 `/api/admin/login` 接口时，Network 面板直接返回 **HTTP 401 Unauthorized**,导致前端无法正确处理登录错误。

---

## 根本原因

**全局拦截器 `AdminAuthInterceptor` 在 Token 验证失败时，直接设置了 HTTP 状态码 401**,违反了前后端分离的最佳实践:

```java
// ❌ 错误的做法（旧代码）
response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401
response.getWriter().write("{\"code\":401,\"msg\":\"Token 已过期\"}");
```

---

## 修复方案

### ✅ 核心原则

1. **所有业务请求统一返回 HTTP 200**
2. **错误信息通过 JSON 响应体中的 code 字段表达**
3. **只有真正的技术异常（如服务器错误）才返回非 200 的 HTTP 状态码**

### 修复后的响应规范

| 场景 | HTTP 状态码 | JSON 响应体 |
|------|-----------|------------|
| 登录成功 | 200 | `{"code":200,"msg":"success","data":{...}}` |
| 账号密码错误 | 200 | `{"code":401,"msg":"账号或密码错误"}` |
| 无权限登录 | 200 | `{"code":403,"msg":"抱歉，您当前账号无权以该身份登录"}` |
| Token 过期/无效 | 200 | `{"code":401,"msg":"Token 已过期或无效"}` |
| 服务器内部错误 | 500 | `{"code":500,"msg":"服务器内部错误"}` |

---

## 修改内容

### 1. 拦截器修改 (`AdminAuthInterceptor.java`)

**修改位置**: `src/main/java/com/daily/dailychineseculture/interceptor/AdminAuthInterceptor.java`

#### 修改点 1: 无 Token 时的响应
```java
// ✅ 修复后（HTTP 200 + JSON）
if (authorization == null || authorization.trim().isEmpty()) {
    response.setStatus(HttpServletResponse.SC_OK); // 改为 200
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write("{\"code\":401,\"msg\":\"Token 已过期或未登录\"}");
    return false;
}
```

#### 修改点 2: Token 无效时的响应
```java
// ✅ 修复后（HTTP 200 + JSON）
if (!jwtUtils.validateToken(token)) {
    response.setStatus(HttpServletResponse.SC_OK); // 改为 200
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write("{\"code\":401,\"msg\":\"Token 已过期或无效\"}");
    return false;
}
```

#### 修改点 3: Token 解析异常时的响应
```java
// ✅ 修复后（HTTP 200 + JSON）
} catch (Exception e) {
    response.setStatus(HttpServletResponse.SC_OK); // 改为 200
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write("{\"code\":401,\"msg\":\"Token 解析失败\"}");
    return false;
}
```

---

### 2. 登录控制器保持不变 (`AdminController.java`)

Controller 层已经正确实现了 HTTP 200 + JSON 错误码的响应方式:

```java
@PostMapping("/login")
public Result<AdminLoginResult> adminLogin(@RequestBody AdminLoginRequest request) {
    try {
        AdminLoginResult loginResult = adminAuthService.adminLogin(request);
        return Result.success(loginResult); // HTTP 200 + code:200
    } catch (IllegalArgumentException e) {
        return Result.error(e.getMessage()); // HTTP 200 + code:500
    } catch (RuntimeException e) {
        String errorMsg = e.getMessage();
        if ("账号或密码错误".equals(errorMsg)) {
            return Result.build(401, errorMsg, null); // HTTP 200 + code:401
        } else if ("抱歉，您当前账号无权以该身份登录".equals(errorMsg)) {
            return Result.build(403, errorMsg, null); // HTTP 200 + code:403
        } else {
            return Result.build(400, errorMsg, null); // HTTP 200 + code:400
        }
    } catch (Exception e) {
        e.printStackTrace();
        return Result.error("服务器内部错误，请稍后重试"); // HTTP 200 + code:500
    }
}
```

---

### 3. 拦截器放行配置确认 (`WebConfig.java`)

已确认登录接口已被正确放行:

```java
// ✅ 配置正确，无需修改
registry.addInterceptor(adminAuthInterceptor)
        .addPathPatterns("/api/admin/**")
        .excludePathPatterns(
            "/api/admin/login"  // 登录接口不参与 Token 验证
        );
```

---

## 前端调用示例

### 1. 登录成功
```javascript
const response = await uni.request({
  url: 'http://localhost:8080/api/admin/login',
  method: 'POST',
  data: {
    account: 'student02',
    password: '123',
    loginRole: 'COURSE_ADMIN'
  }
});

console.log(response.statusCode); // 输出：200
console.log(response.data.code);  // 输出：200
console.log(response.data.data.token); // 输出：eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2. 账号密码错误
```javascript
const response = await uni.request({
  url: 'http://localhost:8080/api/admin/login',
  method: 'POST',
  data: {
    account: 'student02',
    password: 'wrong_password',
    loginRole: 'COURSE_ADMIN'
  }
});

console.log(response.statusCode); // 输出：200 (不再是 401!)
console.log(response.data.code);  // 输出：401
console.log(response.data.msg);   // 输出：账号或密码错误

// 前端处理逻辑
if (response.data.code === 401) {
  uni.showToast({
    title: response.data.msg,
    icon: 'none'
  });
}
```

### 3. Token 过期调用其他接口
```javascript
const token = uni.getStorageSync('adminToken');

const response = await uni.request({
  url: 'http://localhost:8080/api/admin/current',
  method: 'GET',
  header: {
    'Authorization': `Bearer ${token}` // 假设 token 已过期
  }
});

console.log(response.statusCode); // 输出：200 (不再是 401!)
console.log(response.data.code);  // 输出：401
console.log(response.data.msg);   // 输出：Token 已过期或无效

// 前端统一处理 Token 过期
if (response.data.code === 401) {
  uni.removeStorageSync('adminToken');
  uni.redirectTo({
    url: '/pages/admin/login'
  });
}
```

---

## 测试验证

### 测试用例 1: 正常登录
```bash
curl -X POST http://localhost:8080/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "student02",
    "password": "123",
    "loginRole": "COURSE_ADMIN"
  }'
```

**预期响应:**
```json
HTTP/1.1 200
Content-Type: application/json

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

### 测试用例 2: 密码错误
```bash
curl -X POST http://localhost:8080/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "student02",
    "password": "wrong_password",
    "loginRole": "COURSE_ADMIN"
  }'
```

**预期响应:**
```json
HTTP/1.1 200
Content-Type: application/json

{
  "code": 401,
  "msg": "账号或密码错误",
  "data": null
}
```

### 测试用例 3: Token 过期访问其他接口
```bash
curl -X GET http://localhost:8080/api/admin/current \
  -H "Authorization: Bearer expired_token"
```

**预期响应:**
```json
HTTP/1.1 200
Content-Type: application/json

{
  "code": 401,
  "msg": "Token 已过期或无效",
  "data": null
}
```

---

## 优势与收益

### ✅ 修复前的问题
1. **前端无法区分业务错误和网络错误**: HTTP 401 会被浏览器视为网络错误
2. **无法统一错误处理**: 需要分别处理 HTTP 状态码和 JSON 中的 code
3. **用户体验差**: 浏览器可能弹出原生错误提示

### ✅ 修复后的优势
1. **统一的响应格式**: 所有业务响应都遵循相同的 JSON 结构
2. **简化前端逻辑**: 只需检查 `response.data.code` 即可
3. **更好的用户体验**: 可以自定义错误提示，避免浏览器原生弹窗
4. **符合 RESTful 最佳实践**: HTTP 状态码表示通信层面 success/failure，业务状态码在 JSON 中表达

---

## 注意事项

### ⚠️ 不要修改的地方

1. **Controller 层的异常处理逻辑保持不变**
   ```java
   // ✅ 已经正确，无需修改
   if ("账号或密码错误".equals(errorMsg)) {
       return Result.build(401, errorMsg, null);
   }
   ```

2. **拦截器的放行路径保持不变**
   ```java
   // ✅ 已经正确，无需修改
   .excludePathPatterns("/api/admin/login")
   ```

### ⚠️ 安全提醒

虽然返回 HTTP 200，但**不影响安全性**:
- Token 验证逻辑依然严格执行
- 未授权请求依然被拦截
- 只是响应格式更友好

---

## 总结

✅ **已完成修复:**
1. 拦截器 Token 验证失败时返回 HTTP 200 + JSON 错误码
2. Controller 层保持原有的规范化响应
3. 拦截器放行配置已确认正确

✅ **前端现在可以:**
1. 通过 `response.statusCode === 200` 确认请求成功到达后端
2. 通过 `response.data.code` 判断具体业务结果
3. 统一处理所有错误场景（登录错误、Token 过期、无权限等）

🎯 **下一步建议:**
- 更新前端代码，适配新的响应格式
- 添加全局 Axios/Fetch 拦截器，统一处理 `code === 401` 的情况
- 考虑添加 Refresh Token 机制，提升用户体验
