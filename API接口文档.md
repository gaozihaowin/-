# 用户登录API接口文档

## 接口概述
实现用户登录功能，支持已有用户登录和新用户自动注册。

## 接口详情

### 基本信息
- **接口名称**: 用户登录
- **请求方式**: POST
- **请求路径**: `/api/auth/login`
- **Content-Type**: application/json

### 请求参数
```json
{
  "username": "admin",
  "password": "123"
}
```

**参数说明**:
- `username` (String, 必填): 用户名
- `password` (String, 必填): 密码

### 响应格式

#### 成功响应 (200)
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.xxxxx.fake_signature",
    "userInfo": {
      "name": "致良知学员",
      "avatar": "http://example.com/avatar.jpg"
    }
  }
}
```

#### 错误响应 (500)
```json
{
  "code": 500,
  "msg": "错误信息",
  "data": null
}
```

## 业务逻辑

### 1. 参数验证
- 用户名不能为空
- 密码不能为空

### 2. 登录处理流程
1. **预设账户**: 用户名`admin`，密码`123`直接登录成功
2. **已有用户**: 查询数据库验证用户名和密码
3. **新用户**: 自动创建用户并登录成功

### 3. Token生成
- 返回模拟的JWT token字符串
- 格式: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.{随机字符串}.fake_signature_for_demo`

## 使用示例

### CURL请求示例
```bash
# 管理员登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123"}'

# 普通用户登录（不存在则自动创建）
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'

# 错误请求示例
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"","password":"123"}'
```

### JavaScript/Fetch示例
```javascript
const loginData = {
  username: 'admin',
  password: '123'
};

fetch('/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(loginData)
})
.then(response => response.json())
.then(data => {
  if (data.code === 200) {
    console.log('登录成功');
    console.log('Token:', data.data.token);
    console.log('用户信息:', data.data.userInfo);
  } else {
    console.error('登录失败:', data.msg);
  }
})
.catch(error => {
  console.error('请求错误:', error);
});
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 500 | 业务错误 |

## 错误消息说明

| 错误消息 | 说明 |
|----------|------|
| 用户名不能为空 | username字段为空或空白 |
| 密码不能为空 | password字段为空或空白 |
| 用户名或密码错误 | 用户存在但密码不匹配 |
| 用户创建失败 | 新用户创建过程中出现异常 |

## 注意事项

1. **安全性**: 当前token为模拟生成，生产环境需使用真正的JWT实现
2. **密码存储**: 生产环境应对密码进行加密存储
3. **并发处理**: 系统已考虑多用户并发登录场景
4. **数据库依赖**: 功能依赖数据库连接，需确保数据库服务正常

## 测试用例

### 已验证的测试场景
1. ✅ admin/123 登录成功
2. ✅ 空用户名参数校验
3. ✅ 空密码参数校验  
4. ✅ 新用户自动注册
5. ✅ 返回正确的token格式和用户信息