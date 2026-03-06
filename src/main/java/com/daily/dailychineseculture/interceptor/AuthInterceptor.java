package com.daily.dailychineseculture.interceptor;

import com.daily.dailychineseculture.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

/**
 * JWT Token 认证拦截器
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 在请求处理之前进行调用
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是方法处理（比如是静态资源），则直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 获取请求方法
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        // 检查方法或类是否有免认证注解（如果需要可以自定义）
        // if (method.isAnnotationPresent(PublicApi.class) || 
        //     handlerMethod.getBeanType().isAnnotationPresent(PublicApi.class)) {
        //     return true;
        // }

        // 从请求头获取 Authorization
        String authorization = request.getHeader("Authorization");
        
        // 如果没有 Authorization，返回 401
        if (authorization == null || authorization.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录\",\"data\":null}");
            return false;
        }

        // 提取 token（去掉 "Bearer " 前缀）
        String token = authorization;
        if (authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }

        // 验证 token 是否有效
        if (!jwtUtils.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期，请重新登录\",\"data\":null}");
            return false;
        }

        // 可以在这里将用户信息存入 request，方便后续使用
        Long userId = jwtUtils.getUserIdFromToken(token);
        request.setAttribute("userId", userId);

        return true;
    }
}
