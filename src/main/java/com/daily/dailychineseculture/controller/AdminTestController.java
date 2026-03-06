package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * PC 端后台管理测试控制器
 * 用于演示如何从请求上下文中获取用户信息
 */
@RestController
@RequestMapping("/api/admin")
public class AdminTestController {
    
    /**
     * 获取当前登录管理员信息
     * GET /api/admin/current
     * 
     * @param request HTTP 请求（包含解析后的用户信息）
     * @return 当前用户信息
     */
    @GetMapping("/current")
    public Result getCurrentAdmin(HttpServletRequest request) {
        // 从 request attribute 中获取拦截器存入的用户信息
        Long userId = (Long) request.getAttribute("userId");
        String currentRole = (String) request.getAttribute("currentRole");
        Integer campId = (Integer) request.getAttribute("campId");
        
        System.out.println("=== 当前管理员信息 ===");
        System.out.println("userId: " + userId);
        System.out.println("currentRole: " + currentRole);
        System.out.println("campId: " + campId);
        
        return Result.successMsg("获取成功");
    }
}
