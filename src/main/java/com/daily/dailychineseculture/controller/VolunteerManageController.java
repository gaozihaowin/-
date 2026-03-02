package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.MemberManageDTO;
import com.daily.dailychineseculture.dto.DutyAssignmentDTO;
import com.daily.dailychineseculture.service.VolunteerManageService;
import com.daily.dailychineseculture.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 志愿者管理控制器
 */
@RestController
@RequestMapping("/volunteer")
public class VolunteerManageController {

    @Autowired
    private VolunteerManageService volunteerManageService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 获取用户的所有管理范围（对应前端 getVolunteerScopes）
     */
    @GetMapping("/scopes")
    public ResponseResult<List<Map<String, Object>>> getManagementScopes(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));

            // 从数据库查询真实的管理范围
            List<Map<String, Object>> scopes = volunteerManageService.getManagementScopes(userId);

            return ResponseResult.success(scopes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取管理范围失败：" + e.getMessage());
        }
    }

    /**
     * 获取管理成员信息（对应前端 getVolunteerMembers）
     */
    @GetMapping("/manage/members")
    public ResponseResult<MemberManageDTO> getMemberManageInfo(@RequestHeader("Authorization") String token,
                                                               @RequestParam(value = "assignmentId", required = false) Integer assignmentId) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));

            // 调用Service层获取真实数据
            MemberManageDTO result = volunteerManageService.getMemberManageInfo(userId, assignmentId);

            return ResponseResult.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取管理成员信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取分配岗位信息（对应前端 getDutyAssignment）
     */
    @GetMapping("/manage/duty-assignment")
    public ResponseResult<DutyAssignmentDTO> getDutyAssignmentInfo(@RequestHeader("Authorization") String token,
                                                                   @RequestParam(value = "assignmentId", required = false) Integer assignmentId) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));

            // 调用Service层获取真实数据
            DutyAssignmentDTO result = volunteerManageService.getDutyAssignmentInfo(userId, assignmentId);

            return ResponseResult.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取分配岗位信息失败：" + e.getMessage());
        }
    }

    /**
     * 分配岗位（对应前端 assignDuty）
     */
    @PostMapping("/manage/assign-duty")
    public ResponseResult<String> assignDuty(@RequestHeader("Authorization") String token,
                                             @RequestBody Map<String, Object> request) {
        try {
            Long managerUserId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));

            Long targetUserId = ((Number) request.get("targetUserId")).longValue();
            String targetType = (String) request.get("targetType");
            Integer targetId = (Integer) request.get("targetId");
            String dutyType = (String) request.get("dutyType");

            if (targetUserId == null || targetType == null || targetId == null || dutyType == null) {
                return ResponseResult.error("参数不完整");
            }

            boolean success = volunteerManageService.assignDuty(managerUserId, targetUserId, targetType, targetId, dutyType);
            if (success) {
                return ResponseResult.success("分配岗位成功");
            } else {
                return ResponseResult.error("分配岗位失败，用户可能已担任该职位");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("分配岗位失败：" + e.getMessage());
        }
    }

    /**
     * 移除岗位（对应前端 removeDuty）
     */
    @PostMapping("/manage/remove-duty")
    public ResponseResult<String> removeDuty(@RequestHeader("Authorization") String token,
                                             @RequestBody Map<String, Object> request) {
        try {
            Long managerUserId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            Integer assignmentId = (Integer) request.get("assignmentId");

            if (assignmentId == null) {
                return ResponseResult.error("assignmentId不能为空");
            }

            boolean success = volunteerManageService.removeDuty(managerUserId, assignmentId);
            if (success) {
                return ResponseResult.success("移除岗位成功");
            } else {
                return ResponseResult.error("移除岗位失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("移除岗位失败：" + e.getMessage());
        }
    }
}