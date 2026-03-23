# 三个 Profile 接口检查结果

## 一、接口存在性检查

### 1. GET /api/admin/profile → 获取个人信息

**✅ 已存在** - AdminController.java 第 132-151 行

```java
@GetMapping("/profile")
public com.daily.dailychineseculture.common.ResponseResult<Map<String, Object>> getProfile(HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    User user = userMapper.selectById(userId);
    if (user == null) {
        return com.daily.dailychineseculture.common.ResponseResult.error(404, "用户不存在");
    }
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("userId", String.valueOf(user.getUserId()));
    data.put("account", user.getAccount());
    data.put("nickname", user.getNickname());
    data.put("avatar", user.getAvatar());
    data.put("phone", user.getPhone());
    data.put("region", user.getRegion());
    data.put("birthday", user.getBirthday() != null
        ? new SimpleDateFormat("yyyy-MM-dd").format(user.getBirthday()) : null);
    data.put("profession", user.getProfession());
    data.put("gender", user.getGender());
    return com.daily.dailychineseculture.common.ResponseResult.success("查询成功", data);
}
```

---

### 2. PUT /api/admin/profile → 更新个人信息

**✅ 已存在** - AdminController.java 第 153-175 行

```java
@PutMapping("/profile")
public com.daily.dailychineseculture.common.ResponseResult<String> updateProfile(
        @RequestBody Map<String, Object> body,
        HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    User user = userMapper.selectById(userId);
    if (user == null) {
        return com.daily.dailychineseculture.common.ResponseResult.error(404, "用户不存在");
    }
    if (body.get("nickname") != null) user.setNickname((String) body.get("nickname"));
    if (body.get("avatar") != null) user.setAvatar((String) body.get("avatar"));
    if (body.get("phone") != null) user.setPhone((String) body.get("phone"));
    if (body.get("region") != null) user.setRegion((String) body.get("region"));
    if (body.get("profession") != null) user.setProfession((String) body.get("profession"));
    if (body.get("gender") != null) user.setGender(((Number) body.get("gender")).intValue());
    if (body.get("birthday") != null && !((String) body.get("birthday")).isEmpty()) {
        try {
            user.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse((String) body.get("birthday")));
        } catch (Exception ignored) {}
    }
    userMapper.update(user);
    return com.daily.dailychineseculture.common.ResponseResult.success("保存成功");
}
```

---

### 3. PUT /api/admin/profile/password → 修改密码

**✅ 已存在** - AdminController.java 第 177-198 行

```java
@PutMapping("/profile/password")
public com.daily.dailychineseculture.common.ResponseResult<String> updatePassword(
        @RequestBody Map<String, String> body,
        HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    User user = userMapper.selectById(userId);
    if (user == null) {
        return com.daily.dailychineseculture.common.ResponseResult.error(404, "用户不存在");
    }
    String oldPwd = body.get("oldPassword");
    String newPwd = body.get("newPassword");
    String confirmPwd = body.get("confirmPassword");
    if (!user.getPassword().equals(oldPwd)) {
        return com.daily.dailychineseculture.common.ResponseResult.error(400, "当前密码错误");
    }
    if (newPwd == null || newPwd.length() < 6) {
        return com.daily.dailychineseculture.common.ResponseResult.error(400, "新密码不能少于6位");
    }
    if (!newPwd.equals(confirmPwd)) {
        return com.daily.dailychineseculture.common.ResponseResult.error(400, "两次密码不一致");
    }
    user.setPassword(newPwd);
    userMapper.update(user);
    return com.daily.dailychineseculture.common.ResponseResult.success("密码修改成功");
}
```

---

## 二、WebConfig 拦截配置检查

**文件：WebConfig.java**

```java
registry.addInterceptor(adminAuthInterceptor)
        .addPathPatterns("/api/admin/**")  // 拦截所有后台管理接口
        .excludePathPatterns(
            "/admin/login",         // 排除管理员登录接口（无 /api 前缀）
            "/api/admin/login",     // 排除管理员登录接口
            "/captcha",             // 排除验证码接口（通用）
            "/admin/captcha",       // 排除验证码接口（无 /api 前缀）
            "/api/admin/captcha",   // 排除验证码接口（带 /api 前缀）
            "/api/admin/camps/options",  // 排除营期选项（登录页需要）
            "/api/admin/camps/hot",      // 排除热门营期（登录页需要）
            "/api/admin/camps/all"       // 排除全部营期（登录页需要）
        );
```

### 结论

| 接口 | 是否需要登录验证 | 排除状态 |
|------|-----------------|---------|
| GET /api/admin/profile | ✅ 需要 | ❌ 未排除 |
| PUT /api/admin/profile | ✅ 需要 | ❌ 未排除 |
| PUT /api/admin/profile/password | ✅ 需要 | ❌ 未排除 |

**三个接口均正确要求登录验证，拦截配置无误。**

---

## 三、总结

- 三个 profile 接口 **全部存在**
- 拦截器正确配置为拦截 `/api/admin/**`，且只排除了登录、验证码、公开课程列表等少数白名单路径
- profile 系列接口**均需要 JWT Token 认证**，未在排除列表中