package com.daily.dailychineseculture;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

/**
 * API测试客户端
 */
public class ApiTestClient {
    
    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Daily Chinese Culture - API测试客户端");
        System.out.println("========================================");
        System.out.println();

        // 测试1: 管理员登录
        testAdminLogin();
        
        // 测试2: 新用户注册
        testNewUserRegistration();
        
        // 测试3: 空用户名
        testEmptyUsername();
        
        // 测试4: 空密码
        testEmptyPassword();
    }

    private static void testAdminLogin() {
        System.out.println("=== 测试1: 管理员登录 (admin/123) ===");
        String json = "{\"username\":\"admin\",\"password\":\"123\"}";
        sendPostRequest("/api/auth/login", json);
        System.out.println();
    }

    private static void testNewUserRegistration() {
        System.out.println("=== 测试2: 新用户注册登录 ===");
        String username = "testuser" + System.currentTimeMillis();
        String json = "{\"username\":\"" + username + "\",\"password\":\"123456\"}";
        sendPostRequest("/api/auth/login", json);
        System.out.println();
    }

    private static void testEmptyUsername() {
        System.out.println("=== 测试3: 空用户名测试 ===");
        String json = "{\"username\":\"\",\"password\":\"123\"}";
        sendPostRequest("/api/auth/login", json);
        System.out.println();
    }

    private static void testEmptyPassword() {
        System.out.println("=== 测试4: 空密码测试 ===");
        String json = "{\"username\":\"testuser\",\"password\":\"\"}";
        sendPostRequest("/api/auth/login", json);
        System.out.println();
    }

    private static void sendPostRequest(String endpoint, String jsonBody) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("请求URL: " + BASE_URL + endpoint);
            System.out.println("请求体: " + jsonBody);
            System.out.println("响应状态码: " + response.statusCode());
            System.out.println("响应内容: " + response.body());
            
        } catch (Exception e) {
            System.err.println("请求失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}