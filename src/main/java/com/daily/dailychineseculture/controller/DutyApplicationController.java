package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.DutyApplicationSubmitDTO;
import com.daily.dailychineseculture.service.DutyApplicationService;
import com.daily.dailychineseculture.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 权限申请控制器
 * 注意：为配合小程序端调用，该 Controller 路径严禁带有 "/api" 前缀
 */
@RestController
@RequestMapping("/duty-application")
public class DutyApplicationController {

    @Autowired
    private DutyApplicationService dutyApplicationService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 提交权限申请
     * 
     * 接口路径：POST /duty-application/submit
     * 
     * 请求头：
     * - Content-Type: application/json
     * - Authorization: Bearer <token>
     * 
     * 请求体示例：
     * {
     *   "dutyType": "COURSE_ADMIN",
     *   "applyReason": "我是卓越一小队的成员，申请协助管理课程排课与学员作业。"
     * }
     * 
     * 成功响应：
     * {
     *   "code": 200,
     *   "message": "申请提交成功，请耐心等待管理员审核",
     *   "data": null
     * }
     * 
     * 失败响应（被拦截）：
     * {
     *   "code": 400,
     *   "message": "您有待审核的同类申请，请勿重复提交",
     *   "data": null
     * }
     */
    @PostMapping("/submit")
    public ResponseResult<Void> submitApplication(
            @RequestHeader("Authorization") String token,
            @Validated @RequestBody DutyApplicationSubmitDTO dto) {
        
        try {
            // 从 JWT Token 中解析真实用户ID
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));

            // 调用服务层提交申请（包含双重防呆校验）
            dutyApplicationService.submitApplication(userId, dto);

            return ResponseResult.success("申请提交成功，请耐心等待管理员审核", null);
        } catch (RuntimeException e) {
            // 业务异常（防呆拦截）
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            // 系统异常
            return ResponseResult.error(500, "系统异常，请稍后重试");
        }
    }
}
