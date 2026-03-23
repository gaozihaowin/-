# 用户信息更新 API接口文档

## 接口概述

该接口用于登录用户完善或更新个人信息，包括头像、手机号、性别、生日、地域、职业等字段。

**核心安全机制**: 
- ✅ 从 Token 中解析用户 ID，防止越权操作
- ✅ 手机号唯一性校验，避免重复绑定
- ✅ 事务保证数据一致性

---

## 请求信息

### 基本信息

- **接口路径**: `POST /user/update`
- **Content-Type**: `application/json`
- **需要认证**: ✅ 是（必须在 Header 中携带 Token）

### 请求头 (Headers)

```
Authorization: Bearer <你的用户Token>
Content-Type: application/json
```

**⚠️ 极度重要**: 
- `Authorization` 是必需的，后端靠这个识别是谁在提交数据
- Token 通过登录接口获取
- **绝对不能由前端传入 user_id**，后端从 Token 中解析

### 请求体 (Body - JSON)

```json
{
  "avatar": "http://localhost:8080/uploads/20260306_avatar_abc123.jpg",
  "phone": "13812345678",
  "gender": 1,
  "birthday": "1990-05-20",
  "region": "北京",
  "profession": "IT 工程师"
}
```

**请求参数说明**:

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| avatar | String | 否 | 头像 URL，通常为上传接口返回的路径 |
| phone | String | 否 | 手机号，有唯一约束，不能与其他账号重复 |
| gender | Integer | 否 | 性别：0-未知，1-男，2-女 |
| birthday | String | 否 | 生日，格式：yyyy-MM-dd |
| region | String | 否 | 地域，例如：北京、上海 |
| profession | String | 否 | 职业，例如：IT 工程师、教师 |

**注意**: 
- 所有字段都是可选的，但至少要提供一个字段
- 未提供的字段将保持数据库中的原值
- `gender` 对应数据库的 `tinyint` 类型
- `birthday` 对应数据库的 `date` 类型

---

## 响应信息

### 成功响应 (200)

```json
{
  "code": 200,
  "msg": "信息保存成功",
  "data": null,
  "message": "信息保存成功",
  "timestamp": 1741234567890
}
```

**响应字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码，200 表示成功 |
| msg | String | 响应消息 |
| data | Object | 返回数据（此处为 null） |
| message | String | 与 msg 相同，兼容性考虑 |
| timestamp | Long | 响应时间戳 |

### 失败响应

#### 1. 未登录 (401)

```json
{
  "code": 401,
  "msg": "未登录或登录已过期",
  "data": null
}
```

#### 2. 手机号重复 (400) ⚠️

```json
{
  "code": 400,
  "msg": "该手机号已被其他账号绑定",
  "data": null
}
```

#### 3. 用户不存在 (400)

```json
{
  "code": 400,
  "msg": "用户不存在",
  "data": null
}
```

#### 4. 参数校验失败 (400)

```json
{
  "code": 400,
  "msg": "具体的错误信息",
  "data": null
}
```

#### 5. 服务器内部错误 (500)

```json
{
  "code": 500,
  "msg": "服务器内部错误：xxx",
  "data": null
}
```

---

## 使用示例

### 前端调用示例 (UniApp)

```javascript
// 假设已经获取到 token
const token = 'eyJhbGciOiJIUzI1NiJ9...';

// 准备更新的数据
const updateData = {
  avatar: 'http://localhost:8080/uploads/20260306_avatar_xxx.jpg',
  phone: '13812345678',
  gender: 1,
  birthday: '1990-05-20',
  region: '北京',
  profession: 'IT 工程师'
};

// 发送更新请求
uni.request({
  url: 'http://localhost:8080/user/update',
  method: 'POST',
  header: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
  },
  data: updateData,
  success: function(res) {
    const result = res.data;
    
    if (result.code === 200) {
      console.log('信息保存成功');
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      });
      // 可以跳转到其他页面或刷新用户信息
    } else {
      console.error('保存失败:', result.msg);
      uni.showToast({
        title: result.msg,
        icon: 'none'
      });
      
      // 特殊处理手机号重复的情况
      if (result.msg && result.msg.includes('手机号已被其他账号绑定')) {
        // 提示用户更换手机号
      }
    }
  },
  fail: function(err) {
    console.error('网络错误:', err);
    uni.showToast({
      title: '网络错误，请稍后重试',
      icon: 'none'
    });
  }
});
```

### 使用 Postman 测试

1. **设置请求方法和 URL**
   - Method: POST
   - URL: `http://localhost:8080/user/update`

2. **添加 Headers**
   - Key: `Authorization`
     - Value: `Bearer <your_token_here>`
   - Key: `Content-Type`
     - Value: `application/json`

3. **设置 Body**
   - 选择 `raw`
   - 选择 `JSON`
   - 输入 JSON 数据：
   ```json
   {
     "avatar": "http://localhost:8080/uploads/avatar_test.jpg",
     "phone": "13812345678",
     "gender": 1,
     "birthday": "1990-05-20",
     "region": "北京",
     "profession": "工程师"
   }
   ```

4. **发送请求**

5. **查看响应**
   - 成功后返回 `code: 200, msg: "信息保存成功"`

---

## 技术实现细节

### 后端核心流程

```
1. 请求到达 → 2. Token 验证 → 3. 解析用户 ID → 
4. 手机号重复检查 → 5. 执行更新 → 6. 返回结果
```

### 安全控制 (防越权)

**关键代码逻辑**:
```java
// 从请求属性中获取用户 ID（由认证拦截器设置）
Long userId = (Long) httpRequest.getAttribute("userId");

// 绝对不能使用前端传来的 userId
// ❌ 错误做法：Long userId = request.getUserId();
// ✅ 正确做法：从 Token 中解析
```

**认证拦截器** (`AuthInterceptor`):
- 验证 `Authorization` Header
- 解析 JWT Token 获取 userId
- 将 userId 存入 `request.setAttribute("userId", userId)`
- Controller 直接使用，无需再次验证

### 手机号唯一性校验

**Service 层校验逻辑**:
```java
// 检查手机号是否与数据库中其他人冲突
if (StringUtils.hasText(request.getPhone())) {
    User userWithSamePhone = userMapper.selectByPhone(request.getPhone());
    if (userWithSamePhone != null && !userWithSamePhone.getUserId().equals(userId)) {
        throw new DuplicateKeyException("该手机号已被其他账号绑定");
    }
}
```

**数据库约束**:
```sql
ALTER TABLE t_user ADD UNIQUE KEY uk_phone (phone);
```

### 事务处理

**Service 层注解**:
```java
@Transactional(rollbackFor = Exception.class)
public boolean updateUserInfo(Long userId, UserUpdateRequest request) {
    // 所有更新操作在同一个事务中
    // 任何异常都会回滚
}
```

---

## 数据库表结构 (t_user)

### 相关字段

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| user_id | BIGINT | PRIMARY KEY | 用户 ID（主键） |
| avatar | VARCHAR(500) | | 头像 URL |
| phone | VARCHAR(20) | UNIQUE | 手机号（唯一） |
| gender | TINYINT | DEFAULT 0 | 性别：0-未知，1-男，2-女 |
| birthday | DATE | | 生日 |
| region | VARCHAR(100) | | 地域 |
| profession | VARCHAR(100) | | 职业 |

### SQL 更新语句

```sql
UPDATE t_user 
SET avatar = #{avatar}, 
    phone = #{phone}, 
    gender = #{gender}, 
    birthday = #{birthday}, 
    region = #{region}, 
    profession = #{profession} 
WHERE user_id = #{userId}
```

---

## 典型使用场景

### 场景 1: 新用户注册后完善信息

1. 用户通过账号密码登录
2. 系统返回 `isComplete: false`（信息不完整）
3. 前端跳转到信息补全页面
4. 用户填写手机号、性别等信息
5. 调用本接口保存
6. 后续登录时 `isComplete: true`

### 场景 2: 用户上传头像后更新

1. 用户选择头像图片
2. 调用 `/common/upload` 上传接口
3. 获得图片 URL：`http://localhost:8080/uploads/xxx.jpg`
4. 调用本接口，设置 `avatar` 字段
5. 保存成功

### 场景 3: 修改手机号

1. 用户进入账户设置页面
2. 修改手机号字段
3. 调用本接口
4. 如果新手机号已被占用，返回错误提示
5. 否则保存成功

---

## 常见问题

### Q: 为什么不能由前端传入 user_id？

A: 
- **安全性**: 如果允许前端传入 userId，恶意用户可以伪造请求修改他人信息
- **越权攻击**: 攻击者可以遍历 userId 批量修改数据
- **正确做法**: 从 Token 中解析，Token 由后端签发且不可伪造

### Q: 手机号重复如何处理？

A: 
- 后端会捕获 `DuplicateKeyException`
- 返回 `{ "code": 400, "msg": "该手机号已被其他账号绑定" }`
- 前端应提示用户更换其他手机号

### Q: 部分字段更新怎么做？

A: 
- 只传需要更新的字段即可
- 例如只更新手机号：`{ "phone": "13812345678" }`
- 未传的字段保持数据库原值

### Q: Token 过期了怎么办？

A: 
- 接口返回 401 状态码
- 前端应跳转到登录页面重新登录
- 登录后重新获取 Token 再试

### Q: 如何测试本接口？

A: 
1. 先调用登录接口获取 Token
2. 在 Postman 中设置 Authorization Header
3. 构造 JSON 请求体
4. 发送 POST 请求到 `/user/update`
5. 查看响应结果

---

## 异常处理策略

### 已知异常（优雅处理）

| 异常类型 | 返回码 | 提示信息 | 处理方式 |
|---------|-------|---------|---------|
| 未登录 | 401 | "未登录或登录已过期" | 跳转登录 |
| 手机号重复 | 400 | "该手机号已被其他账号绑定" | 提示更换 |
| 用户不存在 | 400 | "用户不存在" | 联系管理员 |
| 参数错误 | 400 | 具体错误信息 | 修正参数 |

### 未知异常（兜底处理）

| 异常类型 | 返回码 | 提示信息 | 处理方式 |
|---------|-------|---------|---------|
| 服务器内部错误 | 500 | "服务器内部错误" | 记录日志，排查原因 |

---

## 版本历史

- **v1.0.0** (2026-03-03): 初始版本
  - 实现用户信息更新功能
  - 集成 JWT Token 认证
  - 手机号唯一性校验
  - 事务管理

---

## 相关文件

### 后端文件

1. **Controller**: [`UserController.java`](file://c:\Users\chenxiao\testProject\JavaTongyi\daily-chinese-studies\src\main\java\com\daily\dailychineseculture\controller\UserController.java)
2. **Service**: [`UserService.java`](file://c:\Users\chenxiao\testProject\JavaTongyi\daily-chinese-studies\src\main\java\com\daily\dailychineseculture\service\UserService.java)
3. **DTO**: [`UserUpdateRequest.java`](file://c:\Users\chenxiao\testProject\JavaTongyi\daily-chinese-studies\src\main\java\com\daily\dailychineseculture\dto\UserUpdateRequest.java)
4. **Mapper**: [`UserMapper.java`](file://c:\Users\chenxiao\testProject\JavaTongyi\daily-chinese-studies\src\main\java\com\daily\dailychineseculture\mapper\UserMapper.java)
5. **拦截器**: [`AuthInterceptor.java`](file://c:\Users\chenxiao\testProject\JavaTongyi\daily-chinese-studies\src\main\java\com\daily\dailychineseculture\interceptor\AuthInterceptor.java)

### 数据库

- 表名：`t_user`
- 唯一约束：`UNIQUE KEY uk_phone (phone)`

---

## 注意事项

1. **Token 有效期**: Token 有效期为 7 天，过期需重新登录
2. **手机号格式**: 建议前端做格式校验（11 位数字）
3. **日期格式**: birthday 必须为 `yyyy-MM-dd` 格式
4. **并发控制**: 数据库唯一约束保证并发安全
5. **日志记录**: 所有更新操作都有详细日志输出
6. **事务回滚**: 任何异常都会触发事务回滚，保证数据一致性
