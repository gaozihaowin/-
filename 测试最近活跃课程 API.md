# 最近活跃课程 API 测试指南

## 📋 测试前准备

### 1. 确认服务状态
✅ 服务已启动 - PID: 25052  
✅ 端口监听 - 8080  
✅ 接口路径 - GET /api/admin/dashboard/recent-camps

---

## 🔧 测试步骤

### 方法一：使用登录接口获取 Token（推荐）

#### Step 1: 先登录获取有效 Token

**PowerShell 命令：**
```powershell
# 1. 调用登录接口
$loginBody = @{
    account = "admin"
    password = "123456"
    loginRole = "SUPER_ADMIN"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body $loginBody

# 2. 提取 Token
$token = $response.data.token
Write-Host "获取到的 Token: $token"

# 3. 使用 Token 访问仪表盘接口
$headers = @{"Authorization" = "Bearer $token"}
$result = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/dashboard/recent-camps" `
    -Headers $headers `
    -Method Get

# 4. 显示结果
$result | ConvertTo-Json -Depth 10
```

**预期输出：**
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "campId": 102,
      "campName": "【致良知线上课堂】笃行班",
      "instructor": "致良知教研团队",
      "visitCount": 5,
      "statusCode": 0,
      "statusText": "待开课",
      "startTime": "2026-03-01 00:00:00"
    }
  ]
}
```

---

### 方法二：Postman 测试

#### 请求配置

1. **请求方式**: GET
2. **请求 URL**: `http://localhost:8080/api/admin/dashboard/recent-camps`
3. **Headers**:
   ```
   Authorization: Bearer YOUR_TOKEN_HERE
   ```

#### 步骤

1. 打开 Postman
2. 创建新的 GET 请求
3. 输入 URL: `http://localhost:8080/api/admin/dashboard/recent-camps`
4. 点击 "Headers" 标签
5. 添加 Header:
   - Key: `Authorization`
   - Value: `Bearer <你的 token>`
6. 点击 "Send"

---

### 方法三：cURL 命令（如果使用 Git Bash）

```bash
# 1. 先登录获取 token
TOKEN=$(curl -X POST http://localhost:8080/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{"account":"admin","password":"123456","loginRole":"SUPER_ADMIN"}' \
  | jq -r '.data.token')

# 2. 使用 token 访问仪表盘接口
curl -X GET http://localhost:8080/api/admin/dashboard/recent-camps \
  -H "Authorization: Bearer $TOKEN"
```

---

## ⚠️ 可能的错误及解决方案

### 错误 1: 401 Token 已过期或未登录
```
原因：Token 无效或已过期
解决：重新调用 /api/admin/login 获取新 Token
```

### 错误 2: 500 获取最新课程失败
```
可能原因：
1. 数据库中没有 t_camp 表数据
2. 缺少 enroll_count 字段

解决方案：
-- 检查表是否存在
SELECT * FROM t_camp LIMIT 5;

-- 如果缺少 enroll_count 字段，执行：
ALTER TABLE t_camp ADD COLUMN enroll_count INT DEFAULT 0 COMMENT '报名人数';
```

### 错误 3: 返回空数组 []
```
原因：数据库中没有营期数据
解决：插入测试数据
```

---

## 📊 测试数据准备（可选）

如果数据库是空的，可以插入一些测试数据：

```sql
-- 插入测试营期数据
INSERT INTO t_camp (type_id, name, intro, start_time, end_time, status, enroll_count) VALUES
(1, '【致良知线上课堂】笃行班', '笃行班介绍', '2026-03-01 00:00:00', '2026-06-01 00:00:00', 0, 5),
(1, '【致良知线上课堂】诚意班', '诚意班介绍', '2025-12-20 00:00:00', '2026-03-20 00:00:00', 1, 573),
(1, '【致良知线上课堂】正心班', '正心班介绍', '2025-11-01 00:00:00', '2026-02-01 00:00:00', 1, 420),
(1, '【致良知线上课堂】格物班', '格物班介绍', '2025-10-15 00:00:00', '2026-01-15 00:00:00', 1, 380),
(1, '【致良知线上课堂】知行班', '知行班介绍', '2025-09-01 00:00:00', '2025-12-01 00:00:00', 2, 650),
(1, '【致良知线上课堂】致知班', '致知班介绍', '2025-08-01 00:00:00', '2025-11-01 00:00:00', 2, 520);
```

---

## ✅ 验证清单

- [ ] 服务正在运行（端口 8080）
- [ ] 拥有有效的 JWT Token
- [ ] 请求头包含 Authorization
- [ ] 数据库 t_camp 表存在且有数据
- [ ] t_camp 表包含 enroll_count 字段

---

## 🎯 成功响应特征

1. **HTTP 状态码**: 200
2. **JSON code**: 200
3. **data 数组**: 最多 5 条记录
4. **排序**: 按 startTime 倒序（最新的在前）
5. **必填字段**: 所有 7 个字段都存在

---

## 🔍 调试技巧

### 查看后台日志

服务正在后台运行，可以在终端看到实时日志：
```
2026-03-07T21:59:45.392+08:00  INFO - Initializing Spring DispatcherServlet
2026-03-07T21:59:45.394+08:00  INFO - Completed initialization in 0 ms
```

如果看到 SQL 错误或数据库连接错误，说明需要检查数据库配置。

---

## 📝 完整的 PowerShell 测试脚本

将以下内容保存为 `test-dashboard.ps1`：

```powershell
# 测试仪表盘 API
Write-Host "=== 开始测试最近活跃课程 API ===" -ForegroundColor Green

try {
    # Step 1: 登录
    Write-Host "`n[1/3] 正在登录..." -ForegroundColor Yellow
    $loginBody = @{
        account = "admin"
        password = "123456"
        loginRole = "SUPER_ADMIN"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody

    if ($loginResponse.code -eq 200) {
        Write-Host "✓ 登录成功" -ForegroundColor Green
        $token = $loginResponse.data.token
    } else {
        throw "登录失败：$($loginResponse.msg)"
    }

    # Step 2: 访问仪表盘接口
    Write-Host "`n[2/3] 正在获取最近活跃课程..." -ForegroundColor Yellow
    $headers = @{"Authorization" = "Bearer $token"}
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/dashboard/recent-camps" `
        -Headers $headers `
        -Method Get

    # Step 3: 显示结果
    Write-Host "`n[3/3] 返回结果:" -ForegroundColor Yellow
    $result | ConvertTo-Json -Depth 10

    Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
    
} catch {
    Write-Host "`n✗ 测试失败：$($_.Exception.Message)" -ForegroundColor Red
}
```

运行方式：
```powershell
.\test-dashboard.ps1
```

---

**文档更新时间**: 2026-03-07  
**API 版本**: v1.0
