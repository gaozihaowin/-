package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.ShortcutDTO;
import com.daily.dailychineseculture.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘控制器
 * 提供仪表相关的 RESTful API
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * 获取仪表盘快捷入口列表
     * GET /api/admin/dashboard/shortcuts
     * 
     * @return 统一响应结果，包含快捷入口列表
     */
    @GetMapping("/shortcuts")
    public ResponseResult<List<ShortcutDTO>> getShortcuts() {
        List<ShortcutDTO> shortcuts = dashboardService.getShortcuts();
        return ResponseResult.success("操作成功", shortcuts);
    }
}
