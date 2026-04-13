package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.DutyApplicationReviewDTO;
import com.daily.dailychineseculture.service.AdminDutyApplicationService;
import com.daily.dailychineseculture.vo.AdminDutyApplicationListItemVO;
import com.daily.dailychineseculture.vo.AdminDutyApplicationStatsVO;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端权限申请控制器
 * 
 * 路由规范：PC 端管理后台接口，必须使用 /api/admin 前缀
 * 数据隔离：基于 Token 解析的 currentRole 实现角色权限隔离
 */
@RestController
@RequestMapping("/api/admin/duty-application")
public class AdminDutyApplicationController {

    @Autowired
    private AdminDutyApplicationService adminDutyApplicationService;

    /**
     * 审批数据统计看板
     * 
     * 接口路径：GET /api/admin/duty-application/stats
     * 
     * 数据隔离逻辑：
     * - SUPER_ADMIN：统计所有申请
     * - 其他角色（如 COURSE_ADMIN）：仅统计自己角色权限范围内的申请
     * 
     * 成功响应：
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "total": 120,
     *     "pending": 5,
     *     "passed": 100,
     *     "rejected": 15
     *   }
     * }
     */
    @GetMapping("/stats")
    public ResponseResult<AdminDutyApplicationStatsVO> getStats(HttpServletRequest request) {
        try {
            // 从拦截器注入的上下文中获取当前管理员角色
            // AdminAuthInterceptor 已将 currentRole 存入 request attribute
            String currentRole = (String) request.getAttribute("currentRole");

            // 校验角色是否存在
            if (currentRole == null || currentRole.trim().isEmpty()) {
                return ResponseResult.error(403, "无法获取当前用户角色");
            }

            AdminDutyApplicationStatsVO stats = adminDutyApplicationService.getStats(currentRole);
            return ResponseResult.success(stats);
        } catch (Exception e) {
            return ResponseResult.error(500, "获取统计数据失败：" + e.getMessage());
        }
    }

    /**
     * 审批分页列表查询
     * 
     * 接口路径：GET /api/admin/duty-application/list
     * 
     * 请求参数：
     * - page: 页码（默认 1）
     * - size: 每页条数（默认 10）
     * - status: 状态过滤（可选，0-待审核, 1-已通过, 2-未通过）
     * - dutyType: 权限类型过滤（可选，仅超级管理员有效）
     * 
     * 数据隔离逻辑：
     * - SUPER_ADMIN：可查看所有申请，可用 dutyType 参数筛选
     * - 其他角色：只能查看 duty_type 与自己角色匹配的申请
     * 
     * 成功响应：
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "total": 5,
     *     "list": [...],
     *     "pageNum": 1,
     *     "pageSize": 10
     *   }
     * }
     */
    @GetMapping("/list")
    public ResponseResult<PageInfo<AdminDutyApplicationListItemVO>> getApplicationList(
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "dutyType", required = false) String dutyType) {

        try {
            // 从拦截器注入的上下文中获取当前管理员角色
            String currentRole = (String) request.getAttribute("currentRole");

            // 校验角色是否存在
            if (currentRole == null || currentRole.trim().isEmpty()) {
                return ResponseResult.error(403, "无法获取当前用户角色");
            }

            // 参数校验：分页参数
            if (page == null || page < 1) {
                page = 1;
            }
            if (size == null || size < 1 || size > 100) {
                size = 10;
            }

            PageInfo<AdminDutyApplicationListItemVO> pageInfo = adminDutyApplicationService.getApplicationList(
                    currentRole,
                    page,
                    size,
                    status,
                    dutyType
            );

            return ResponseResult.success(pageInfo);
        } catch (Exception e) {
            return ResponseResult.error(500, "获取列表数据失败：" + e.getMessage());
        }
    }

    /**
     * 审批流转接口（通过/拒绝）
     * 
     * 接口路径：POST /api/admin/duty-application/review
     * 
     * 请求体：
     * {
     *   "applyId": 16,
     *   "status": 1,          // 1-通过, 2-拒绝
     *   "auditRemark": "同意！" // 拒绝时必填，通过时选填
     * }
     * 
     * 数据隔离逻辑：
     * - SUPER_ADMIN：可审批所有申请
     * - 其他角色：只能审批 duty_type 与自己角色匹配的申请
     * 
     * 成功响应：
     * {
     *   "code": 200,
     *   "message": "审批完成",
     *   "data": null
     * }
     */
    @PostMapping("/review")
    public ResponseResult<Void> reviewApplication(
            HttpServletRequest request,
            @RequestBody DutyApplicationReviewDTO reviewDTO) {

        try {
            // 从拦截器注入的上下文中获取当前管理员信息
            Long userId = (Long) request.getAttribute("userId");
            String currentRole = (String) request.getAttribute("currentRole");

            // 校验用户信息
            if (userId == null) {
                return ResponseResult.error(403, "无法获取当前用户ID");
            }
            if (currentRole == null || currentRole.trim().isEmpty()) {
                return ResponseResult.error(403, "无法获取当前用户角色");
            }

            // 参数校验
            if (reviewDTO == null) {
                return ResponseResult.error(400, "请求参数不能为空");
            }
            if (reviewDTO.getApplyId() == null) {
                return ResponseResult.error(400, "申请ID不能为空");
            }
            if (reviewDTO.getStatus() == null) {
                return ResponseResult.error(400, "审批状态不能为空");
            }
            if (reviewDTO.getStatus() != 1 && reviewDTO.getStatus() != 2) {
                return ResponseResult.error(400, "审批状态只能为1(通过)或2(拒绝)");
            }

            // 调用服务层执行审批
            adminDutyApplicationService.reviewApplication(userId, currentRole, reviewDTO);

            return ResponseResult.success("审批完成", null);
        } catch (RuntimeException e) {
            // 业务异常（越权、状态不对等）返回 400
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            return ResponseResult.error(500, "审批处理失败：" + e.getMessage());
        }
    }
}
