# 文件上传 API接口文档

## 接口概述

该接口用于用户上传头像图片到本地服务器，支持 jpg、png、jpeg 格式，最大 5MB。

---

## 请求信息

### 基本信息

- **接口路径**: `POST /common/upload`
- **Content-Type**: `multipart/form-data`
- **需要认证**: ✅ 是（需要在 Header 中携带 Token）

### 请求头 (Headers)

```
Authorization: Bearer <你的用户 Token>
Content-Type: multipart/form-data
```

**注意**: 
- `Authorization` 是必需的，用于验证用户身份
- Token 通过登录接口获取

### 请求体 (Body)

表单字段：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 二进制文件流，字段名必须为 `file` |

**文件限制**:
- 支持格式：jpg、png、jpeg
- 最大大小：5MB (5,242,880 bytes)

---

## 响应信息

### 成功响应 (200)

```json
{
  "code": 200,
  "msg": "上传成功",
  "data": "http://localhost:8080/uploads/20260306_avatar_abc123.jpg",
  "message": "上传成功",
  "timestamp": 1741234567890
}
```

**响应字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码，200 表示成功 |
| msg | String | 响应消息 |
| data | String | 图片的完整 HTTP 访问路径 |
| message | String | 与 msg 相同，兼容性考虑 |
| timestamp | Long | 响应时间戳 |

### 失败响应

#### 1. 未登录 (401)

```json
{
  "code": 401,
  "msg": "未登录",
  "data": null
}
```

#### 2. 登录过期 (401)

```json
{
  "code": 401,
  "msg": "登录已过期，请重新登录",
  "data": null
}
```

#### 3. 文件为空 (400)

```json
{
  "code": 400,
  "msg": "上传文件不能为空",
  "data": null
}
```

#### 4. 文件格式不支持 (400)

```json
{
  "code": 400,
  "msg": "不支持的文件类型，仅支持 jpg、png、jpeg 格式",
  "data": null
}
```

#### 5. 文件超出大小限制 (400)

```json
{
  "code": 400,
  "msg": "文件大小超过限制（最大 5MB）",
  "data": null
}
```

#### 6. 服务器内部错误 (500)

```json
{
  "code": 500,
  "msg": "文件上传失败：xxx",
  "data": null
}
```

---

## 使用示例

### 前端调用示例 (UniApp)

```javascript
// 假设已经获取到 token
const token = 'eyJhbGciOiJIUzI1NiJ9...';

// 选择图片并上传
uni.chooseImage({
  count: 1,
  sizeType: ['compressed'],
  sourceType: ['album', 'camera'],
  success: function(res) {
    const filePath = res.tempFilePaths[0];
    
    // 创建上传任务
    uni.uploadFile({
      url: 'http://localhost:8080/common/upload',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: function(uploadRes) {
        const result = JSON.parse(uploadRes.data);
        
        if (result.code === 200) {
          console.log('上传成功，图片 URL:', result.data);
          // 可以将 result.data 保存到用户信息中
          // 例如：更新用户头像字段
        } else {
          console.error('上传失败:', result.msg);
          uni.showToast({
            title: result.msg,
            icon: 'none'
          });
        }
      },
      fail: function(err) {
        console.error('上传失败:', err);
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        });
      }
    });
  }
});
```

### 使用 Postman 测试

1. **设置请求方法和 URL**
   - Method: POST
   - URL: `http://localhost:8080/common/upload`

2. **添加 Headers**
   - Key: `Authorization`
   - Value: `Bearer <your_token_here>`

3. **设置 Body**
   - 选择 `form-data`
   - 添加一个字段：
     - Key: `file` (类型选择 `File`)
     - Value: 选择一个本地图片文件

4. **发送请求**

5. **查看响应**
   - 成功后会返回图片的访问 URL

---

## 配置说明

### application.yml 配置

```yaml
# 文件上传配置
file:
  upload-dir: C:/camp_system/uploads/    # 文件保存路径（Windows）
  max-size: 5242880                       # 最大文件大小（字节），默认 5MB
```

**注意**: 
- `upload-dir` 路径末尾建议加上 `/`
- Linux/Mac 系统路径示例：`/var/uploads/`
- 确保应用有该目录的读写权限

### 静态资源映射配置

WebConfig 配置类已将 `/uploads/**` 路径映射到本地物理目录：

```java
registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + uploadDir);
```

这样前端就可以通过 `http://localhost:8080/uploads/文件名.jpg` 直接访问上传的图片。

---

## 文件命名规则

上传的文件会自动重命名为：

```
时间戳_avatar_UUID.扩展名
```

示例：
```
20260306143025_avatar_a1b2c3d4e5f6g7h8i9j0.jpg
```

**优点**:
- 避免文件名冲突
- 包含时间信息，便于管理
- 保留原始文件扩展名

---

## 安全说明

1. **Token 验证**: 接口已集成 JWT Token 验证，只有登录用户才能上传文件
2. **文件类型限制**: 只允许上传图片格式（jpg/png/jpeg），防止恶意文件上传
3. **文件大小限制**: 限制最大 5MB，防止服务器存储空间被快速占用
4. **文件存储**: 文件保存在配置的本地目录，确保该目录不在 Web 根目录下

---

## 常见问题

### Q: 上传后无法访问图片？

A: 检查以下几点：
1. 确认 `application.yml` 中的 `upload-dir` 路径正确
2. 确认该目录存在且有读写权限
3. 确认 WebConfig 中的资源映射配置正确
4. 检查防火墙是否阻止了 8080 端口

### Q: 如何修改上传文件大小限制？

A: 在 `application.yml` 中修改 `max-size` 值（单位：字节）：
```yaml
file:
  max-size: 10485760  # 改为 10MB
```

### Q: 想取消 Token 验证怎么办？

A: 在 `WebConfig.java` 的 `addInterceptors` 方法中添加排除路径：
```java
.excludePathPatterns(
    "/common/upload",  // 添加这行来排除上传接口的 Token 验证
    ...
)
```

---

## 技术实现

### 后端技术栈
- Spring Boot 4.0.2
- Spring MVC MultipartFile
- JWT Token 认证
- WebMvcConfigurer 静态资源映射

### 核心组件
1. **CommonController**: 处理文件上传请求
2. **AuthInterceptor**: JWT Token 认证拦截器
3. **WebConfig**: 静态资源映射和拦截器配置

---

## 版本历史

- **v1.0.0** (2026-03-03): 初始版本
  - 实现基础文件上传功能
  - 集成 JWT Token 认证
  - 支持本地文件存储和 HTTP 访问
