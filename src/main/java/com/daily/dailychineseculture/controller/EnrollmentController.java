package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.service.EnrollmentService;
import com.daily.dailychineseculture.util.JwtUtils;
import com.daily.dailychineseculture.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 学员报名检查控制器
 * 为 C 端小程序提供报名鉴权接口
 */
@RestController
@RequestMapping("/enrollment")
public class EnrollmentController {
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * 检查用户是否已报名指定营期
     * 请求路径：GET /enrollment/check
     * 
     * @param token JWT 令牌（从请求头获取）
     * @param campId 营期 ID（从查询参数获取）
     * @return true-已报名，false-未报名
     */
    @GetMapping("/check")
    public Result<Boolean> checkEnrollment(
            @RequestHeader("Authorization") String token,
            @RequestParam Integer campId) {
        
        try {
            // 1. 解析 Token 获取当前登录用户 ID
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            
            // 2. 参数校验
            if (campId == null) {
                return Result.error("营期 ID 不能为空");
            }
            
            // 3. 调用 Service 检查报名状态
            Boolean isEnrolled = enrollmentService.checkEnrollment(userId, campId);
            
            // 4. 返回结果
            return Result.success(isEnrolled);
            
        } catch (RuntimeException e) {
            return Result.build(401, "未授权：" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error("检查报名状态失败：" + e.getMessage());
        }
    }
}
