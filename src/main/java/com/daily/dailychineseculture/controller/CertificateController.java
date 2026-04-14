package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.entity.Certificate;
import com.daily.dailychineseculture.service.CertificateService;
import com.daily.dailychineseculture.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 适配SpringBoot 21/3.x，用jakarta包
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    // 严格匹配你的工具类名：JwtUtils
    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/my-list")
    public Result<List<Certificate>> getMyCertificates(HttpServletRequest request) {
        // 1. 获取请求头中的Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("请先登录");
        }

        // 2. 去除Bearer前缀
        String token = authHeader.replace("Bearer ", "");

        // 3. 调用你工具类的原生方法，完全匹配！
        Long userId = jwtUtils.getUserIdFromToken(token);

        // 4. 查询并返回数据
        List<Certificate> certList = certificateService.getMyCertificates(userId);
        return Result.success(certList);
    }

    @GetMapping("/user")
    public Result<List<Certificate>> getUserCertificates(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("请先登录");
        }
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(token);
        List<Certificate> certList = certificateService.getMyCertificates(userId);
        return Result.success(certList);
    }
}