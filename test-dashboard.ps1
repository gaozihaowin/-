# 测试最近活跃课程 API
Write-Host "=== 测试最近活跃课程 API ===" -ForegroundColor Green

try {
    # Step 1: 登录获取 Token
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
    if ($_.ErrorDetails.Message) {
        Write-Host "详情：$($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}
