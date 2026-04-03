package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.service.AdminDutyApplicationService;
import com.daily.dailychineseculture.vo.AdminListItemVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端人员管理控制器
 * 
 * 路由规范：PC 端管理后台接口，必须使用 /api/admin 前缀
 * 数据隔离：基于 Token 解析的 currentRole 实现角色权限隔离
 */
@RestController
@RequestMapping("/api/admin/management")
public class ManagementController {

    @Autowired
    private AdminDutyApplicationService adminDutyApplicationService;

    /**
     * 获取管理人员列表
     * 
     * 接口路径：GET /api/admin/management/admin-list
     * 
     * 数据隔离逻辑：
     * - SUPER_ADMIN：查询所有已赋权的管理员
     * - COURSE_ADMIN：只能查询 duty_type = 'COURSE_ADMIN' 的管理员
     * - ARCHIVE_ADMIN：只能查询 duty_type = 'ARCHIVE_ADMIN' 的管理员
     * 
     * 成功响应：
     * {
     *   "code": 200,
     *   "message": "查询成功",
     *   "data": [
     *     {
     *       "userId": 2026000001,
     *       "nickname": "上海银",
     *       "account": "student01",
     *       "dutyType": "COURSE_ADMIN",
     *       "assignTime": "2026-03-25T14:00:00"
     *     }
     *   ]
     * }
     */
    @GetMapping("/admin-list")
    public ResponseResult<List<AdminListItemVO>> getAdminList(HttpServletRequest request) {
        try {
            // 从拦截器注入的上下文中获取当前管理员角色
            String currentRole = (String) request.getAttribute("currentRole");

            // 校验角色是否存在
            if (currentRole == null || currentRole.trim().isEmpty()) {
                return ResponseResult.error(403, "无法获取当前用户角色");
            }

            List<AdminListItemVO> adminList = adminDutyApplicationService.getAdminList(currentRole);
            return ResponseResult.success(adminList);
        } catch (Exception e) {
            return ResponseResult.error(500, "查询管理人员列表失败：" + e.getMessage());
        }
    }
}
