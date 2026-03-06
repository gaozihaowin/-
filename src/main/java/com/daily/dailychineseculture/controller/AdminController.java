package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.dto.AdminLoginRequest;
import com.daily.dailychineseculture.dto.AdminLoginResult;
import com.daily.dailychineseculture.service.AdminAuthService;
import com.daily.dailychineseculture.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * PC 端后台管理控制器
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private AdminAuthService adminAuthService;
    
    /**
     * 管理员登录接口
     * POST /api/admin/login
     * 
     * @param request 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<AdminLoginResult> adminLogin(@RequestBody AdminLoginRequest request) {
        try {
            AdminLoginResult loginResult = adminAuthService.adminLogin(request);
            return Result.success(loginResult);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (RuntimeException e) {
            // 处理特定的业务异常
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
