package com.daily.dailychineseculture.config;

import com.daily.dailychineseculture.interceptor.AuthInterceptor;
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
        // 注册认证拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns(
                    "/login",             // 排除登录接口
                    "/wxLogin",           // 排除微信登录接口
                    "/user/register",     // 排除注册接口
                    "/error",             // 排除错误页面
                    "/courses/hot"        // 排除热门课程接口 (首页公开访问)
                );
    }
}
