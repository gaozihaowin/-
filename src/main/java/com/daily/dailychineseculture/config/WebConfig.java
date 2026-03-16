package com.daily.dailychineseculture.config;

import com.daily.dailychineseculture.interceptor.AuthInterceptor;
import com.daily.dailychineseculture.interceptor.AdminAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类 - 用于配置静态资源映射和拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private AuthInterceptor authInterceptor;
    
    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    /**
     * 添加静态资源处理器
     * 将 /uploads/** 请求映射到本地物理路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置文件上传目录的静态资源映射
        // 访问路径：http://localhost:8080/uploads/文件名.jpg
        // 实际物理路径：C:/camp_system/uploads/文件名.jpg
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
        
        // 可以添加其他静态资源映射，例如：
        // registry.addResourceHandler("/static/**")
        //         .addResourceLocations("classpath:/static/");
    }

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册认证拦截器（移动端 C 端用户）
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")  // 默认拦截所有路径
                .excludePathPatterns(
                    // ========== 认证相关：完全公开 ==========
                    "/login",                        // 账号密码登录
                    "/wxLogin",                      // 微信登录
                    "/user/register",                // 用户注册
                    "/user/updateAll",               // 用户信息更新（公开接口）
                    "/updateUserInfo",               // 兼容旧版信息更新
                    
                    // ========== 首页与展示数据：完全公开 ==========
                    "/courses/hot",                  // 热门课程推荐（小程序端首页）
                    "/courses/*/schedule",           // 课程安排目录（营期详情页）
                    "/courses/*/info",               // 营期详情信息（课程详情页顶部信息栏）
                    "/api/admin/camps/options",      // 营期选项列表（PC 端登录页）
                    "/api/admin/camps/hot",          // 热门营期列表
                    "/api/admin/camps/all",          // 全部营期列表
                    
                    // ========== 静态资源与系统页面 ==========
                    "/error",                        // 错误页面
                    "/favicon.ico",                  // 网站图标
                    "/swagger-ui/**",                // Swagger 文档
                    "/v3/api-docs/**",               // OpenAPI 文档
                    "/static/**",                    // 静态资源
                    "/public/**",                    // 公共资源
                    "/webjars/**",                   // WebJars 资源
                    
                    // ========== OPTIONS 预检请求 ==========
                    "//**"                          // 允许跨域 OPTIONS 请求
                );
                
        // 注册 PC 端后台管理鉴权拦截器
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**")  // 拦截所有后台管理接口
                .excludePathPatterns(
                    "/api/admin/login",     // 排除管理员登录接口
                    "/api/admin/camps/options",  // 排除营期选项（登录页需要）
                    "/api/admin/camps/hot",      // 排除热门营期（登录页需要）
                    "/api/admin/camps/all"       // 排除全部营期（登录页需要）
                );
    }
}
