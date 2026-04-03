package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.DutyApplicationSubmitDTO;
import com.daily.dailychineseculture.dto.RevokeApplicationDTO;
import com.daily.dailychineseculture.service.DutyApplicationService;
import com.daily.dailychineseculture.util.JwtUtils;
import com.daily.dailychineseculture.vo.DutyApplicationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     */
    @PostMapping("/submit")
    public ResponseResult<Void> submitApplication(
            @RequestHeader("Authorization") String token,
            @Validated @RequestBody DutyApplicationSubmitDTO dto) {
        
        try {
            // 从 JWT Token 中解析真实用户ID
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));

            dutyApplicationService.submitApplication(userId, dto);

            return ResponseResult.success("申请提交成功，请耐心等待管理员审核", null);
        } catch (RuntimeException e) {
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            return ResponseResult.error(500, "系统异常，请稍后重试");
        }
    }

    /**
     * 获取我的申请列表
     * 
     * 接口路径：GET /duty-application/my-list
     * 
     * 请求头：
     * - Authorization: Bearer <token>
     * 
     * 成功响应：
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": [
     *     {
     *       "applyId": 15,
     *       "dutyType": "COURSE_ADMIN",
     *       "applyReason": "申请协助排课",
     *       "status": 0,
     *       "auditRemark": null,
     *       "createTime": "2026-04-03T10:00:00"
     *     }
     *   ]
     * }
     */
    @GetMapping("/my-list")
    public ResponseResult<List<DutyApplicationVO>> getMyApplicationList(
            @RequestHeader("Authorization") String token) {
        
        try {
            // 从 JWT Token 中解析真实用户ID
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));

            List<DutyApplicationVO> list = dutyApplicationService.getMyApplicationList(userId);

            return ResponseResult.success(list);
        } catch (RuntimeException e) {
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            return ResponseResult.error(500, "系统异常，请稍后重试");
        }
    }

    /**
     * 撤销申请
     * 
     * 接口路径：POST /duty-application/revoke
     * 
     * 请求头：
     * - Content-Type: application/json
     * - Authorization: Bearer <token>
     * 
     * 请求体示例：
     * {
     *   "applyId": 16
     * }
     * 
     * 成功响应：
     * {
     *   "code": 200,
     *   "message": "撤销成功",
     *   "data": null
     * }
     */
    @PostMapping("/revoke")
    public ResponseResult<Void> revokeApplication(
            @RequestHeader("Authorization") String token,
            @Validated @RequestBody RevokeApplicationDTO dto) {
        
        try {
            // 从 JWT Token 中解析真实用户ID
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));

            dutyApplicationService.revokeApplication(userId, dto);

            return ResponseResult.success("撤销成功", null);
        } catch (RuntimeException e) {
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            return ResponseResult.error(500, "系统异常，请稍后重试");
        }
    }
}
