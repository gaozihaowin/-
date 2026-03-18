package com.daily.dailychineseculture.interceptor;

import com.daily.dailychineseculture.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * PC 端后台管理鉴权拦截器
 * 拦截所有 /api/admin/** 请求，验证 Token 有效性
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * 拦截请求进行 Token 验证
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行登录接口
        String requestUri = request.getRequestURI();
        if ("/api/admin/login".equals(requestUri)
                || "/admin/login".equals(requestUri)
                || "/captcha".equals(requestUri)
                || "/admin/captcha".equals(requestUri)
                || "/api/admin/captcha".equals(requestUri)) {
            return true;
        }
        
        // 从请求头获取 Authorization
        String authorization = request.getHeader("Authorization");
        
        // 检查是否有 Token
        if (authorization == null || authorization.trim().isEmpty()) {
            // 返回 HTTP 200 + JSON 格式错误信息（而不是 HTTP 401）
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Token 已过期或未登录\"}");
            return false;
        }
        
        // 提取 Bearer Token
        String token = authorization;
        if (authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        
        // 验证 Token 有效性
        if (!jwtUtils.validateToken(token)) {
            // 返回 HTTP 200 + JSON 格式错误信息（而不是 HTTP 401）
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Token 已过期或无效\"}");
            return false;
        }
        
        // 将用户信息存入请求上下文，方便 Controller 层使用
        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            String currentRole = jwtUtils.getCurrentRoleFromToken(token);
            Integer campId = jwtUtils.getCampIdFromToken(token);
            
            // 存入 request attribute
            request.setAttribute("userId", userId);
            request.setAttribute("currentRole", currentRole);
            request.setAttribute("campId", campId);
            
        } catch (Exception e) {
            // 返回 HTTP 200 + JSON 格式错误信息（而不是 HTTP 401）
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Token 解析失败\"}");
            return false;
        }
        
        return true;
    }
    
    /**
     * 请求完成后清理资源（可选）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理 ThreadLocal（如果使用了的话）
        // UserContext.clear();
    }
}
