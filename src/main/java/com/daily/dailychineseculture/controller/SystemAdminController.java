package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.AssignRequest;
import com.daily.dailychineseculture.service.SystemAdminService;
import com.daily.dailychineseculture.vo.AdminStatsVO;
import com.daily.dailychineseculture.vo.AdminUserAggVO;
import com.daily.dailychineseculture.vo.AdminUserDetailVO;
import com.daily.dailychineseculture.vo.SystemAdminVO;
import com.daily.dailychineseculture.vo.UserSearchVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/system-admins")
@RequiredArgsConstructor
public class SystemAdminController {

    private final SystemAdminService systemAdminService;

    @GetMapping("/stats")
    public ResponseResult<AdminStatsVO> getStats() {
        return ResponseResult.success(systemAdminService.getAdminStats());
    }

    @GetMapping("/list")
    public ResponseResult<List<SystemAdminVO>> getAdminList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return ResponseResult.success(
                systemAdminService.getAdminList(keyword, page, pageSize));
    }

    @GetMapping("/list-agg")
    public ResponseResult<List<AdminUserAggVO>> getAdminListAgg(
            @RequestParam(required = false) String keyword) {
        return ResponseResult.success(systemAdminService.getAdminListAgg(keyword));
    }

    @GetMapping("/detail/{userId}")
    public ResponseResult<AdminUserDetailVO> getAdminDetail(@PathVariable Long userId) {
        return ResponseResult.success(systemAdminService.getAdminDetail(userId));
    }

    @GetMapping("/users/search")
    public ResponseResult<List<UserSearchVO>> searchUsers(@RequestParam String keyword) {
        return ResponseResult.success(systemAdminService.searchUsers(keyword));
    }

    @PostMapping("/assign")
    public ResponseResult<Void> assign(@RequestBody @Valid AssignRequest request) {
        systemAdminService.assign(request);
        return ResponseResult.successMsg("授权成功");
    }

    @DeleteMapping("/revoke/{assignmentId}")
    public ResponseResult<Void> revoke(@PathVariable Integer assignmentId) {
        systemAdminService.revoke(assignmentId);
        return ResponseResult.successMsg("撤销成功");
    }
}
