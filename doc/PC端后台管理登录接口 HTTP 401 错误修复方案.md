# PC端后台管理登录接口 HTTP 401 错误修复方案

## 问题描述

前端联调 `/api/admin/login` 接口时，Network 面板返回 **HTTP 401 Unauthorized**。

**根本原因**: 
- 移动端 C 端的 `AuthInterceptor` 拦截器拦截了所有 `/api/**` 路径
- PC端后台管理的登录接口没有被正确放行

---

## 修复方案（已完成）

### ✅ 修复 1: 白名单配置 - 排除 PC端后台管理接口

**文件位置**: `src/main/java/com/daily/dailychineseculture/config/WebConfig.java`

#### 修改前 (❌)
```java
registry.addInterceptor(authInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/login",
            "/wxLogin",
            "/user/register",
            "/error",
            "/courses/hot"
        );
```

#### 修改后 (✅)
```java
registry.addInterceptor(authInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/login",
            "/wxLogin",
            "/user/register",
            "/error",
            "/courses/hot",
            "/api/admin/**"  // ← 新增：排除整个 PC端后台管理路径
        );
```

**效果**: 
- 移动端 C 端拦截器不再拦截 `/api/admin/**` 下的任何请求
- PC端后台管理接口由专门的 `AdminAuthInterceptor` 处理

---

### ✅ 修复 2: 登录鉴权逻辑 - 返回正确的业务错误码

**文件位置**: `src/main/java/com/daily/dailychineseculture/service/impl/AdminAuthServiceImpl.java`

#### 修改内容

**Service 层** (第 69-72 行):
```java
if (dutyInfo == null) {
    // 没有该角色权限 - 返回业务错误码 403
    throw new RuntimeException("无权以该身份登录:403");
}
```

**Controller 层** (第 34-44 行):
```java
} catch (RuntimeException e) {
    String errorMsg = e.getMessage();
    
    // 检查是否包含 403 标记（无权以该身份登录）
    if (errorMsg.contains(":403")) {
        return Result.build(403, errorMsg.replace(":403", ""), null);
    } else if ("账号或密码错误".equals(errorMsg)) {
        return Result.build(401, errorMsg, null);
    } else {
        return Result.build(400, errorMsg, null);
    }
}
```

**效果**:
- 查不到权限记录时，返回 `code: 403, msg: "无权以该身份登录"`
- 密码错误时，返回 `code: 401, msg: "账号或密码错误"`
- 其他错误返回 `code: 400`

---

### ✅ 修复 3: Spring Security 配置（本项目未使用）

**检查结果**: 本项目未引入 Spring Security，无需配置。

如果未来需要添加 Spring Security，请参考以下配置:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // 禁用 CSRF
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/login").permitAll()  // 放行登录接口
                .anyRequest().authenticated()  // 其他接口需要认证
            );
        return http.build();
    }
}
```

---

## 修复后的完整代码

### 1. WebConfig.java (完整代码)

```java
package com.daily.dailychineseculture.config;

import com.daily.dailychineseculture.interceptor.AuthInterceptor;
import com.daily.dailychineseculture.interceptor.AdminAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private AuthInterceptor authInterceptor;
    
    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册认证拦截器（移动端 C 端用户）
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/login",
                    "/wxLogin",
                    "/user/register",
                    "/error",
                    "/courses/hot",
                    "/api/admin/**"  // ← 关键修复：排除 PC端后台管理接口
                );
        
        // 注册 PC端后台管理鉴权拦截器
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns(
                    "/api/admin/login"  // ← 放行登录接口
                );
    }
}
```

### 2. AdminController.java (完整代码)

```java
package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.dto.AdminLoginRequest;
import com.daily.dailychineseculture.dto.AdminLoginResult;
import com.daily.dailychineseculture.service.AdminAuthService;
import com.daily.dailychineseculture.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private AdminAuthService adminAuthService;
    
    @PostMapping("/login")
    public Result<AdminLoginResult> adminLogin(@RequestBody AdminLoginRequest request) {
        try {
            AdminLoginResult loginResult = adminAuthService.adminLogin(request);
            return Result.success(loginResult);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (RuntimeException e) {
            String errorMsg = e.getMessage();
            
            // 检查是否包含 403 标记（无权以该身份登录）
            if (errorMsg.contains(":403")) {
                return Result.build(403, errorMsg.replace(":403", ""), null);
            } else if ("账号或密码错误".equals(errorMsg)) {
                return Result.build(401, errorMsg, null);
            } else {
                return Result.build(400, errorMsg, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误，请稍后重试");
        }
    }
}
```

### 3. AdminAuthServiceImpl.java (关键片段)

```java
@Override
public AdminLoginResult adminLogin(AdminLoginRequest request) {
    // 1. 参数校验
    if (request.getAccount() == null || request.getAccount().trim().isEmpty()) {
        throw new IllegalArgumentException("账号不能为空");
    }
    if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
        throw new IllegalArgumentException("密码不能为空");
    }
    if (request.getLoginRole() == null || request.getLoginRole().trim().isEmpty()) {
        throw new IllegalArgumentException("登录角色不能为空");
    }
    
    // 2. 根据账号查询用户
    User user = userMapper.selectByAccount(request.getAccount());
    if (user == null) {
        throw new RuntimeException("账号或密码错误");
    }
    
    // 3. 校验密码
    if (!request.getPassword().equals(user.getPassword())) {
        throw new RuntimeException("账号或密码错误");
    }
    
    // 4. 查询用户的职位权限
    Map<String, Object> dutyInfo = dutyAssignmentMapper.selectWithCampInfo(
        user.getUserId(), 
        request.getLoginRole()
    );
    
    if (dutyInfo == null) {
        // ← 关键修复：返回 403 错误码
        throw new RuntimeException("无权以该身份登录:403");
    }
    
    // 5. 检查状态是否正常
    Integer status = (Integer) dutyInfo.get("status");
    if (status != null && status != 1) {
        throw new RuntimeException("您的账号已被冻结，请联系管理员");
    }
    
    // 6. 生成 JWT Token
    Integer campId = (Integer) dutyInfo.get("camp_id");
    String token = jwtUtils.generateToken(
        user.getUserId(), 
        user.getAccount(), 
        request.getLoginRole(),
        campId
    );
    
    // 7. 组装返回结果
    AdminLoginResult result = new AdminLoginResult();
    result.setToken(token);
    
    AdminLoginResult.UserInfo userInfo = new AdminLoginResult.UserInfo();
    userInfo.setUserId(String.valueOf(user.getUserId()));
    userInfo.setAccount(user.getAccount());
    userInfo.setNickname(user.getNickname() != null ? user.getNickname() : user.getAccount());
    userInfo.setCurrentRole(request.getLoginRole());
    userInfo.setCampId(campId);
    
    result.setUserInfo(userInfo);
    
    return result;
}
```

---

## 测试验证

### 测试用例 1: 正常登录 ✅
```bash
curl -X POST http://localhost:8080/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "student02",
    "password": "123",
    "loginRole": "COURSE_ADMIN"
  }'
```

**预期响应** (HTTP 200):
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

### 测试用例 2: 密码错误 ✅
```bash
curl -X POST http://localhost:8080/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "student02",
    "password": "wrong_password",
    "loginRole": "COURSE_ADMIN"
  }'
```

**预期响应** (HTTP 200):
```json
{
  "code": 401,
  "msg": "账号或密码错误",
  "data": null
}
```

### 测试用例 3: 无权以该身份登录 ✅
```bash
curl -X POST http://localhost:8080/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "student02",
    "password": "123",
    "loginRole": "ARCHIVE_ADMIN"
  }'
```

**预期响应** (HTTP 200):
```json
{
  "code": 403,
  "msg": "无权以该身份登录",
  "data": null
}
```

### 测试用例 4: 无 Token 访问其他接口 ✅
```bash
curl -X GET http://localhost:8080/api/admin/current
```

**预期响应** (HTTP 200):
```json
{
  "code": 401,
  "msg": "Token 已过期或未登录",
  "data": null
}
```

---

## 前端调用示例

```javascript
// 管理员登录
const loginRes = await uni.request({
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

console.log('HTTP Status:', loginRes.statusCode); // 输出：200 ✅

if (loginRes.data.code === 200) {
  // 登录成功
  const { token, userInfo } = loginRes.data.data;
  uni.setStorageSync('adminToken', token);
  uni.setStorageSync('adminUserInfo', userInfo);
  
  // 跳转到管理后台首页
  uni.redirectTo({ url: '/pages/admin/dashboard' });
  
} else if (loginRes.data.code === 401) {
  // 账号或密码错误
  uni.showToast({
    title: loginRes.data.msg,
    icon: 'none'
  });
  
} else if (loginRes.data.code === 403) {
  // 无权以该身份登录
  uni.showToast({
    title: loginRes.data.msg,
    icon: 'none'
  });
  
} else {
  // 其他错误
  uni.showToast({
    title: loginRes.data.msg,
    icon: 'none'
  });
}
```

---

## 修复总结

### ✅ 已完成的修复

| 修复点 | 文件 | 修改内容 | 效果 |
|--------|------|----------|------|
| **白名单配置** | `WebConfig.java` | 添加 `/api/admin/**` 到排除列表 | 移动端拦截器不再拦截 PC端后台接口 |
| **鉴权逻辑** | `AdminAuthServiceImpl.java` | 抛出异常时带上 `:403` 标记 | Controller 可以识别并返回正确的业务错误码 |
| **错误处理** | `AdminController.java` | 解析 `:403` 标记并返回 code:403 | 前端收到正确的业务状态码 |

### 🎯 核心价值

1. **明确的职责划分**:
   - 移动端 C 端拦截器只处理 `/api/**` (除 `/api/admin/**`)
   - PC端后台拦截器专门处理 `/api/admin/**`

2. **统一的响应规范**:
   - 所有业务错误都返回 HTTP 200 + JSON 错误码
   - `code: 401` → 账号或密码错误
   - `code: 403` → 无权以该身份登录

3. **清晰的错误提示**:
   - 前端可以根据不同的 code 显示不同的提示
   - 用户体验更好

---

## 注意事项

⚠️ **不要删除原有的排除路径**:
```java
.excludePathPatterns(
    "/login",           // ← 保留：移动端登录接口
    "/wxLogin",         // ← 保留：微信登录接口
    "/user/register",   // ← 保留：用户注册接口
    "/error",           // ← 保留：错误页面
    "/courses/hot",     // ← 保留：热门课程接口
    "/api/admin/**"     // ← 新增：PC端后台管理接口
)
```

⚠️ **确保拦截器顺序**:
- `AuthInterceptor` (移动端) 先注册
- `AdminAuthInterceptor` (PC 端) 后注册
- 避免拦截器冲突

---

## 下一步建议

1. **添加登录失败次数限制**: 防止暴力破解
2. **添加图形验证码**: 连续失败 5 次后要求输入验证码
3. **添加账号锁定机制**: 连续失败 N 次后锁定账号 M 分钟
4. **实现 Refresh Token 机制**: 提升用户体验
