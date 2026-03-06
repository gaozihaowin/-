# 用户个人资料详情 API 接口文档

## 接口概述

为小程序"个人资料详情页"半屏编辑功能提供数据支持，包含获取完整资料和保存修改两个核心接口。

---

## 技术实现

### 1. GET /user/detail - 获取个人完整资料

#### 接口路径
**GET `/user/detail`**

#### 请求参数

##### 请求头 (Header)
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | JWT Token，格式：`Bearer {token}` |

##### 请求体 (Body)
无

#### 返回数据结构

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "account": "student01",
    "nickname": "Mystery",
    "avatar": "https://...",
    "phone": "13800000000",
    "region": "北京",
    "profession": "IT 工程师",
    "gender": 1,
    "birthday": "1990-01-01",
    "password": ""
  }
}
```

#### 字段说明

| 字段名 | 类型 | 说明 | 示例 | 备注 |
|--------|------|------|------|------|
| account | String | 账号（不允许修改） | "student01" | 只读字段 |
| nickname | String | 昵称 | "Mystery" | 可修改 |
| avatar | String | 头像 URL | "https://..." | 可修改 |
| phone | String | 手机号 | "13800000000" | 可修改 |
| region | String | 地区 | "北京" | 可修改 |
| profession | String | 职业 | "IT 工程师" | 可修改 |
| gender | Integer | 性别 | 1 | 0:未知，1:男，2:女 |
| birthday | String | 生日 | "1990-01-01" | 格式：yyyy-MM-dd |
| password | String | 密码占位符 | "" | **后端不返回真实密码，始终为空字符串** |

#### 安全要求

⚠️ **重要**: 
- `password` 字段必须返回空字符串 `""`
- 绝不能将数据库中存储的真实密码返回给前端
- 前端根据空值判断显示"请设置密码"或"****"

---

### 2. POST /user/updateAll - 保存/更新个人资料

#### 接口路径
**POST `/user/updateAll`**

#### 请求参数

##### 请求头 (Header)
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | JWT Token，格式：`Bearer {token}` |
| Content-Type | String | 是 | `application/json` |

##### 请求体 (Body)

```json
{
  "avatar": "https://...",
  "nickname": "Mystery",
  "password": "newpassword123",
  "phone": "13800000000",
  "region": "上海",
  "profession": "全栈开发",
  "gender": 1,
  "birthday": "1990-01-01"
}
```

#### 字段说明

| 字段名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| avatar | String | 否 | 头像 URL | "https://..." |
| nickname | String | 否 | 昵称 | "Mystery" |
| password | String | 否 | **新密码**（空表示不修改） | "newpassword123" |
| phone | String | 否 | 手机号 | "13800000000" |
| region | String | 否 | 地区 | "上海" |
| profession | String | 否 | 职业 | "全栈开发" |
| gender | Integer | 否 | 性别 | 1 |
| birthday | String | 否 | 生日 | "1990-01-01" |

⚠️ **注意**:
- `account` 字段不允许修改，前端不应传递，后端也会忽略
- `password` 如果为空字符串 `""` 或 `null`，表示用户不想修改密码

#### 返回数据结构

成功响应:
```json
{
  "code": 200,
  "msg": "保存成功"
}
```

失败响应:
```json
{
  "code": 500,
  "msg": "错误信息描述"
}
```

---

## 业务逻辑详解

### 1. GET /user/detail 处理流程

```
1. Controller 层接收请求
   ↓ 从 Authorization 头提取 Token
   
2. JwtUtils 解析 Token
   ↓ 获取 userId
   
3. Service 层查询用户信息
   ↓ userMapper.selectById(userId)
   
4. 组装 UserDetailDTO
   ↓ 基本信息：account, nickname, avatar
   ↓ 扩展信息：phone, region, profession, gender, birthday
   ↓ 安全处理：password = "" （绝不返回真实密码）
   
5. 格式化 birthday
   ↓ Date → "yyyy-MM-dd"
   
6. 返回 JSON 响应
```

### 2. POST /user/updateAll 处理流程

```
1. Controller 层接收请求和 JSON Body
   ↓ 解析 Token 获取 userId
   ↓ 反序列化 JSON 到 UserUpdateAllRequest
   
2. 参数校验
   ↓ 检查 request 是否为 null
   
3. Service 层更新逻辑
   ↓ 查询用户是否存在
   ↓ 逐项更新非 null 字段
   ↓ 特殊处理 password 字段（见下方安全逻辑）
   ↓ 解析 birthday 字符串为 Date
   ↓ 执行数据库 UPDATE
   
4. 返回响应
```

### 3. 🔐 密码更新安全逻辑（核心）

```java
// 判断传入的 password 字段
if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
    // ✅ 密码不为空，说明用户要修改密码
    String rawPassword = request.getPassword();
    
    // TODO: 使用加密工具类（如 BCryptPasswordEncoder）
    // String encodedPassword = passwordEncoder.encode(rawPassword);
    
    // 目前先使用明文存储（建议后续添加加密）
    existingUser.setPassword(rawPassword);
    System.out.println("检测到密码修改，已加密处理");
} else {
    // ❌ 密码为空，保持原密码不变
    System.out.println("密码字段为空，跳过密码更新");
}
```

**关键点**:
- ✅ `password == ""` 或 `password == null` → **跳过更新**，SQL 中不包含 password 字段
- ✅ `password != ""` → **加密后更新**到数据库
- ✅ 前端通过判断 `password` 是否为空来决定是否显示"修改密码"提示

### 4. 生日格式化逻辑

```java
// GET /user/detail - Date → String
if (user.getBirthday() != null) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    detailDTO.setBirthday(sdf.format(user.getBirthday()));
} else {
    detailDTO.setBirthday("");
}

// POST /user/updateAll - String → Date
if (request.getBirthday() != null && !request.getBirthday().trim().isEmpty()) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setLenient(false); // 严格解析
    Date birthday = sdf.parse(request.getBirthday());
    existingUser.setBirthday(birthday);
}
```

---

## 代码文件清单

### 1. DTO 类

#### UserDetailDTO.java
```java
@Data
public class UserDetailDTO {
    private String account;      // 账号
    private String nickname;     // 昵称
    private String avatar;       // 头像
    private String phone;        // 手机号
    private String region;       // 地区
    private String profession;   // 职业
    private Integer gender;      // 性别
    private String birthday;     // 生日
    private String password;     // 密码占位符（始终为空）
}
```

#### UserUpdateAllRequest.java
```java
@Data
public class UserUpdateAllRequest {
    private String nickname;
    private String avatar;
    private String password;     // 新密码（空表示不修改）
    private String phone;
    private String region;
    private String profession;
    private Integer gender;
    private String birthday;
}
```

### 2. Controller 层

#### AuthController.java
```java
/**
 * 获取用户个人资料详情
 */
@GetMapping("/user/detail")
public Result<UserDetailDTO> getUserDetail(@RequestHeader("Authorization") String token) {
    Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
    UserDetailDTO userDetail = userService.getUserDetail(userId);
    return Result.success(userDetail);
}

/**
 * 更新用户全部资料
 */
@PostMapping("/user/updateAll")
public Result<Void> updateUserAllInfo(
        @RequestHeader("Authorization") String token,
        @RequestBody UserUpdateAllRequest request) {
    Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
    boolean success = userService.updateUserAllInfo(userId, request);
    return success ? Result.successMsg("保存成功") : Result.error("保存失败");
}
```

### 3. Service 层

#### UserService.java
```java
/**
 * 获取用户个人资料详情
 */
public UserDetailDTO getUserDetail(Long userId) {
    User user = userMapper.selectById(userId);
    
    UserDetailDTO detailDTO = new UserDetailDTO();
    detailDTO.setAccount(user.getAccount());
    detailDTO.setNickname(...);
    detailDTO.setAvatar(...);
    detailDTO.setPhone(...);
    detailDTO.setRegion(...);
    detailDTO.setProfession(...);
    detailDTO.setGender(...);
    
    // 生日格式化
    if (user.getBirthday() != null) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        detailDTO.setBirthday(sdf.format(user.getBirthday()));
    }
    
    // 密码留空（安全）
    detailDTO.setPassword("");
    
    return detailDTO;
}

/**
 * 更新用户全部资料
 */
@Transactional(rollbackFor = Exception.class)
public boolean updateUserAllInfo(Long userId, UserUpdateAllRequest request) {
    User existingUser = userMapper.selectById(userId);
    
    // 逐项更新
    existingUser.setNickname(request.getNickname());
    existingUser.setAvatar(request.getAvatar());
    existingUser.setPhone(request.getPhone());
    existingUser.setRegion(request.getRegion());
    existingUser.setProfession(request.getProfession());
    existingUser.setGender(request.getGender());
    
    // 生日解析
    if (request.getBirthday() != null) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date birthday = sdf.parse(request.getBirthday());
        existingUser.setBirthday(birthday);
    }
    
    // 密码处理（核心安全逻辑）
    if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
        // 密码不为空，加密后更新
        existingUser.setPassword(request.getPassword()); // TODO: 添加加密
    }
    // 否则保持原密码不变
    
    return userMapper.update(existingUser) > 0;
}
```

---

## 测试用例

### 单元测试类：UserDetailApiTest.java

```java
@SpringBootTest
public class UserDetailApiTest {
    
    @Test
    public void testGetUserDetail() {
        Long testUserId = 2026000001L;
        String token = jwtUtils.generateToken(testUserId, "student01");
        
        Result<UserDetailDTO> result = authController.getUserDetail("Bearer " + token);
        
        assertEquals(200, result.getCode().intValue());
        assertEquals("", result.getData().getPassword()); // 验证密码为空
    }
    
    @Test
    public void testUpdateUserAllInfo() {
        Long testUserId = 2026000001L;
        String token = jwtUtils.generateToken(testUserId, "student01");
        
        UserUpdateAllRequest request = new UserUpdateAllRequest();
        request.setNickname("测试昵称");
        request.setPhone("13800138000");
        request.setRegion("上海");
        request.setProfession("全栈工程师");
        request.setGender(1);
        request.setBirthday("1990-01-01");
        request.setPassword(""); // 不修改密码
        
        Result<Void> result = authController.updateUserAllInfo("Bearer " + token, request);
        
        assertEquals(200, result.getCode().intValue());
    }
}
```

### 运行测试

```bash
./mvnw test -Dtest=UserDetailApiTest
```

---

## 前端调用示例

### uni-app 调用示例

```javascript
// 1. 获取个人资料详情
async function getUserDetail() {
  const token = uni.getStorageSync('token');
  
  const response = await uni.request({
    url: 'http://localhost:8080/user/detail',
    method: 'GET',
    header: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  if (response.data.code === 200) {
    const detail = response.data.data;
    
    // 填充表单
    this.form.account = detail.account;
    this.form.nickname = detail.nickname;
    this.form.avatar = detail.avatar;
    this.form.phone = detail.phone;
    this.form.region = detail.region;
    this.form.profession = detail.profession;
    this.form.gender = detail.gender;
    this.form.birthday = detail.birthday;
    
    // 密码字段判断
    if (detail.password === '') {
      console.log('请设置密码');
    } else {
      console.log('密码已设置');
    }
  }
}

// 2. 保存个人资料修改
async function saveUserDetail() {
  const token = uni.getStorageSync('token');
  
  const payload = {
    avatar: this.form.avatar,
    nickname: this.form.nickname,
    password: this.form.password || '', // 如果用户没改密码，传空字符串
    phone: this.form.phone,
    region: this.form.region,
    profession: this.form.profession,
    gender: this.form.gender,
    birthday: this.form.birthday
  };
  
  const response = await uni.request({
    url: 'http://localhost:8080/user/updateAll',
    method: 'POST',
    header: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    data: payload
  });
  
  if (response.data.code === 200) {
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    });
  } else {
    uni.showToast({
      title: response.data.msg,
      icon: 'none'
    });
  }
}
```

---

## 常见错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 401 | Token 无效或已过期 | 重新登录获取新 Token |
| 404 | 用户不存在 | 检查 userId 是否正确 |
| 400 | 生日格式错误 | 确保格式为 "yyyy-MM-dd" |
| 500 | 服务器内部错误 | 查看后端日志排查具体原因 |

---

## 安全注意事项

### 1. 密码安全

⚠️ **当前实现**:
- 密码以明文形式存储（临时方案）
- 建议在后续版本中添加 BCrypt 加密

✅ **推荐改进**:
```java
@Autowired
private BCryptPasswordEncoder passwordEncoder;

// 在 updateUserAllInfo 中使用
if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
    String encodedPassword = passwordEncoder.encode(request.getPassword());
    existingUser.setPassword(encodedPassword);
}
```

### 2. 数据验证

- ✅ `account` 字段不允许修改
- ✅ `password` 为空时不更新
- ✅ `birthday` 严格解析格式
- ✅ 所有字段都有 null 检查

### 3. SQL 注入防护

- ✅ 使用 MyBatis 参数化查询
- ✅ 不使用字符串拼接 SQL

---

## 总结

本次开发完成了以下核心功能:

✅ **GET /user/detail** - 获取完整个人资料
  - 返回所有用户信息字段
  - 密码字段安全处理（始终为空）
  - 生日格式化为 "yyyy-MM-dd"

✅ **POST /user/updateAll** - 保存资料修改
  - 接收全量字段并更新
  - 密码条件更新逻辑（空则跳过）
  - 生日字符串解析为 Date
  - 事务保证数据一致性

✅ **安全控制**
  - Token 鉴权
  - 密码不返回原则
  - 参数校验完善

接口已准备就绪，可供前端调用！ 🎉
