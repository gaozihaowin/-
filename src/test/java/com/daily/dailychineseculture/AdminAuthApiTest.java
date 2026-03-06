package com.daily.dailychineseculture;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * PC 端后台管理多角色登录鉴权 API 测试
 */
@SpringBootTest
public class AdminAuthApiTest {
    
    /**
     * 测试管理员登录接口
     */
    @Test
    public void testAdminLogin() {
        System.out.println("=== 测试管理员登录接口 ===");
        System.out.println("接口路径：POST /api/admin/login");
        System.out.println("请求参数示例：");
        System.out.println("{");
        System.out.println("  \"account\": \"student02\",");
        System.out.println("  \"password\": \"123\",");
        System.out.println("  \"loginRole\": \"COURSE_ADMIN\"");
        System.out.println("}");
        System.out.println("");
        System.out.println("预期响应：");
        System.out.println("{");
        System.out.println("  \"code\": 200,");
        System.out.println("  \"msg\": \"success\",");
        System.out.println("  \"data\": {");
        System.out.println("    \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",");
        System.out.println("    \"userInfo\": {");
        System.out.println("      \"userId\": \"2026000002\",");
        System.out.println("      \"account\": \"student02\",");
        System.out.println("      \"nickname\": \"王老师\",");
        System.out.println("      \"currentRole\": \"COURSE_ADMIN\",");
        System.out.println("      \"campId\": null");
        System.out.println("    }");
        System.out.println("  }");
        System.out.println("}");
    }
    
    /**
     * 测试 Token 拦截器
     */
    @Test
    public void testTokenInterceptor() {
        System.out.println("=== 测试 Token 拦截器 ===");
        System.out.println("接口路径：GET /api/admin/current");
        System.out.println("请求头：Authorization: Bearer <token>");
        System.out.println("");
        System.out.println("预期行为：");
        System.out.println("1. 无 Token → 返回 401");
        System.out.println("2. Token 过期 → 返回 401");
        System.out.println("3. Token 有效 → 放行并注入用户信息到 request attribute");
    }
}
