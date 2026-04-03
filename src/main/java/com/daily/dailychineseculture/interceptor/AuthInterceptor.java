package com.daily.dailychineseculture.interceptor;

import com.daily.dailychineseculture.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

/**
 * JWT Token 认证拦截器
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

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

        // 从请求头获取 Authorization
        String authorization = request.getHeader("Authorization");
        
        // 如果没有 Authorization，返回 401
        if (!StringUtils.hasText(authorization)) {
            logger.warn("请求缺少 Authorization 头，URI: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录，请先登录\",\"data\":null}");
            return false;
        }

        // 提取 token（规范化剥离 "Bearer " 前缀）
        String token = authorization;
        if (authorization.toLowerCase().startsWith("bearer ")) {
            token = authorization.substring(7);
        }
        
        // 校验提取后的 Token 是否有效
        if (!StringUtils.hasText(token)) {
            logger.warn("Token 格式无效，URI: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Token格式无效\",\"data\":null}");
            return false;
        }

        // 验证 token 是否有效
        if (!jwtUtils.validateToken(token)) {
            logger.warn("Token 验证失败，URI: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期，请重新登录\",\"data\":null}");
            return false;
        }

        // 解析用户ID并存入 request，方便后续使用
        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            request.setAttribute("userId", userId);
            logger.debug("用户认证成功，userId: {}, URI: {}", userId, request.getRequestURI());
        } catch (Exception e) {
            logger.error("解析用户ID异常: {}，URI: {}", e.getMessage(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Token解析失败，请重新登录\",\"data\":null}");
            return false;
        }

        return true;
    }
}
