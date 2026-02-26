# 登录接口API文档

## 接口概述
POST /login 接口实现了账号密码登录功能，包含用户信息完整性检查，用于判断用户是否需要跳转到信息补全页面。

## 接口详情

### 请求地址
```
POST /login
```

### 请求参数
```json
{
  "username": "student01",
  "password": "123456"
}
```

**参数说明：**
- `username` (String, 必填): 用户账号/手机号
- `password` (String, 必填): 用户密码

### 响应格式

#### 登录成功响应
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjIwMjYwMDAwMDEsInVzZXJuYW1lIjoic3R1ZGVudDAxIiwic3ViIjoic3R1ZGVudDAxIiwiaWF0IjoxNzQwNTUyMjk5LCJleHAiOjE3NDE3NjIyOTl9.XXXXXX",
    "isComplete": false,
    "userInfo": {
      "userid": "2026000001",
      "username": "student01",
      "avatar": "",
      "phone": "13800138000",
      "gender": 0,
      "birthday": ""
    }
  }
}
```

#### 新用户注册成功响应
```json
{
  "code": 201,
  "msg": "注册并登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.XXXXXX",
    "isComplete": false,
    "userInfo": {
      "userid": "2026000002",
      "username": "newuser",
      "avatar": "",
      "phone": "",
      "gender": 0,
      "birthday": ""
    }
  }
}
```

#### 错误响应
```json
{
  "code": 401,
  "msg": "账号或密码错误",
  "data": null
}
```

```json
{
  "code": 500,
  "msg": "登录过程中发生错误: 具体错误信息",
  "data": null
}
```

## 核心业务逻辑

### 信息完整性判断规则
系统会检查以下四个字段来判断用户信息是否完整：

1. **手机号(phone)**: 不能为空字符串
2. **头像(avatar)**: 不能为空字符串  
3. **性别(gender)**: 不能为0（0表示未知）
4. **生日(birthday)**: 不能为null

**判断逻辑：**
- 当以上四个字段都满足条件时，`isComplete = true`
- 任一字段不满足条件时，`isComplete = false`

### 处理流程

1. **参数校验**: 检查username和password是否为空
2. **用户验证**: 查询数据库验证用户是否存在及密码正确性
3. **自动注册**: 用户不存在时自动创建新用户
4. **信息完整性检查**: 检查用户关键信息是否完整
5. **Token生成**: 生成JWT访问令牌
6. **响应返回**: 返回包含token、isComplete状态和用户信息的完整响应

## 前端使用说明

### 响应数据结构
```javascript
{
  code: 200,           // 状态码
  msg: "登录成功",      // 消息
  data: {
    token: "xxx",      // JWT令牌
    isComplete: false, // 信息完整性状态
    userInfo: {        // 用户基本信息
      userid: "2026000001",
      username: "student01", 
      avatar: "",
      phone: "13800138000",
      gender: 0,
      birthday: ""
    }
  }
}
```

### 路由跳转逻辑
```javascript
// 前端接收到登录响应后的处理逻辑
if (response.data.isComplete === true) {
  // 信息完整，跳转到主页面
  uni.switchTab({ url: '/pages/index/index' });
} else {
  // 信息不完整，跳转到信息补全页面
  uni.navigateTo({ url: '/pages/user/profile-complete' });
}
```

## 技术实现要点

### 后端组件
- **Controller层**: `AuthController.login()` 方法处理登录请求
- **Service层**: `UserService.isUserInfoComplete()` 判断信息完整性
- **DTO层**: `LoginResult` 和 `UserInfoDTO` 承载响应数据
- **工具类**: `JwtUtils` 生成和验证JWT令牌

### 数据库字段映射
- `phone` → 手机号
- `avatar` → 头像URL  
- `gender` → 性别 (0:未知, 1:男, 2:女)
- `birthday` → 生日 (Date类型)

### 安全考虑
- 密码采用明文存储（开发阶段）
- JWT令牌有效期7天
- 包含异常捕获和错误处理机制

## 测试用例

### 正常登录场景
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student01","password":"123456"}'
```

### 新用户注册场景  
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser123","password":"password123"}'
```

### 错误场景
```bash
# 缺少参数
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student01"}'

# 密码错误
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student01","password":"wrongpass"}'
```