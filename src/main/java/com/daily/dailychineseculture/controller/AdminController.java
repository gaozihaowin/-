package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.dto.AdminLoginRequest;
import com.daily.dailychineseculture.dto.AdminLoginResult;
import com.daily.dailychineseculture.dto.CampListPageDTO;
import com.daily.dailychineseculture.dto.RecentCampDTO;
import com.daily.dailychineseculture.service.AdminAuthService;
import com.daily.dailychineseculture.service.CampService;
import com.daily.dailychineseculture.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PC 端后台管理控制器
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private AdminAuthService adminAuthService;
    
    @Autowired
    private CampService campService;
    
    /**
     * 管理员登录接口
     * POST /api/admin/login
     * 
     * @param request 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<AdminLoginResult> adminLogin(@RequestBody AdminLoginRequest request) {
        try {
            AdminLoginResult loginResult = adminAuthService.adminLogin(request);
            return Result.success(loginResult);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (RuntimeException e) {
            // 处理特定的业务异常
            String errorMsg = e.getMessage();
            
            // 检查是否包含 403 标记（无权以该身份登录）
            if (errorMsg.contains(":403")) {
                return Result.build(403, errorMsg.replace(":403", ""), null);
            } else if ("账号或密码错误".equals(errorMsg)) {
                return Result.build(401, errorMsg, null);
            } else {
                return Result.build(400, errorMsg, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误，请稍后重试");
        }
    }
    
    /**
     * 获取最近活跃课程列表（仪表盘用）
     * GET /api/admin/dashboard/recent-camps
     * 
     * @return 最近活跃课程列表
     */
    @GetMapping("/dashboard/recent-camps")
    public Result<List<RecentCampDTO>> getRecentCamps() {
        try {
            List<RecentCampDTO> recentCamps = campService.getRecentCamps();
            return Result.success(recentCamps);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取最新课程失败：" + e.getMessage());
        }
    }
    
    /**
     * 分页查询营期列表（营期管理大盘）
     * GET /api/admin/camps
     * 
     * @param page 当前页码（默认 1）
     * @param size 每页大小（默认 10）
     * @param keyword 关键词（可选，模糊匹配营期名称）
     * @param status 状态（可选，精确匹配）
     * @return 分页结果
     */
    @GetMapping("/camps")
    public Result<CampListPageDTO> getCampList(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "size", defaultValue = "10") Integer size,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "status", required = false) Integer status
    ) {
        try {
            CampListPageDTO campList = campService.getCampList(page, size, keyword, status);
            return Result.success(campList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取营期列表失败：" + e.getMessage());
        }
    }
}
