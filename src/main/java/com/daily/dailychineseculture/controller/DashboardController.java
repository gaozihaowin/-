package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.service.DashboardService;
import com.daily.dailychineseculture.vo.SuperAdminDashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/super-admin")
    public ResponseResult<SuperAdminDashboardVO> getSuperAdminDashboard(
            @RequestAttribute("currentRole") String currentRole) {
        if (!"SUPER_ADMIN".equals(currentRole)) {
            return ResponseResult.error(403, "仅总管理员可访问此面板");
        }
        return ResponseResult.success(dashboardService.getSuperAdminDashboard());
    }
}
