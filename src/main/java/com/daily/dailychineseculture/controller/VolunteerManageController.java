package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.MemberManageDTO;
import com.daily.dailychineseculture.dto.DutyAssignmentDTO;
import com.daily.dailychineseculture.service.VolunteerManageService;
import com.daily.dailychineseculture.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
                                                               @RequestParam(value = "assignmentId", required = false) Integer assignmentId,
                                                               @RequestParam(value = "smallGroupId", required = false) Integer smallGroupId) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));

            // 调用Service层获取真实数据
            MemberManageDTO result = volunteerManageService.getMemberManageInfo(userId, assignmentId, smallGroupId);

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

    /**
     * 获得的志愿者证书
     */
    @GetMapping("/certificates/self")
    public ResponseResult<List<Map<String, Object>>> getSelfCertificates(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            List<Map<String, Object>> certificates = volunteerManageService.getCertificatesByUser(userId);
            return ResponseResult.success(certificates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取证书列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取个人获得的所有证书
     */
    @GetMapping("/certificates/all")
    public ResponseResult<List<Map<String, Object>>> getAllSelfCertificates(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            List<Map<String, Object>> certificates = volunteerManageService.getAllCertificatesByUser(userId);
            return ResponseResult.success(certificates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取证书列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/certificate/issue")
    public ResponseResult<?> issueCertificate(
            @RequestBody Map<String, Object> request) {
        try {
            Long volunteerId = ((Number) request.get("volunteerId")).longValue();
            String certificateType = (String) request.get("certificateType");

            Integer assignmentId = null;
            if (request.get("assignmentId") != null) {
                assignmentId = ((Number) request.get("assignmentId")).intValue();
            }
            Long homeworkId = null;
            if (request.get("homeworkId") != null) {
                homeworkId = ((Number) request.get("homeworkId")).longValue();
            }

            boolean exists = volunteerManageService.checkCertificateIssued(volunteerId, certificateType, assignmentId, homeworkId);
            if (exists) return ResponseResult.error("该类型证书已颁发");

            boolean success = volunteerManageService.issueCertificate(volunteerId, certificateType, assignmentId,homeworkId);
            return success ? ResponseResult.success("颁发成功") : ResponseResult.error("颁发失败");
        } catch (Exception e) {
            return ResponseResult.error("颁发失败：" + e.getMessage());
        }
    }

    @PostMapping("/certificate/cancel")
    public ResponseResult<?> cancelCertificate(
            @RequestBody Map<String, Object> request) {
        try {
            // 处理volunteerId参数
            Long volunteerId = null;
            Object volunteerIdObj = request.get("volunteerId");
            if (volunteerIdObj instanceof Number) {
                volunteerId = ((Number) volunteerIdObj).longValue();
            } else if (volunteerIdObj instanceof String) {
                volunteerId = Long.parseLong((String) volunteerIdObj);
            }
            String certificateType = (String) request.get("certificateType");

            Integer assignmentId = null;
            Object assignmentIdObj = request.get("assignmentId");
            if (assignmentIdObj != null) {
                if (assignmentIdObj instanceof Number) {
                    assignmentId = ((Number) assignmentIdObj).intValue();
                } else if (assignmentIdObj instanceof String) {
                    assignmentId = Integer.parseInt((String) assignmentIdObj);
                }
            }
            Long homeworkId = null;
            Object homeworkIdObj = request.get("homeworkId");
            if (homeworkIdObj != null) {
                if (homeworkIdObj instanceof Number) {
                    homeworkId = ((Number) homeworkIdObj).longValue();
                } else if (homeworkIdObj instanceof String) {
                    homeworkId = Long.parseLong((String) homeworkIdObj);
                }
            }

            boolean success = volunteerManageService.cancelCertificate(volunteerId, certificateType, assignmentId, homeworkId);
            return success ? ResponseResult.success("取消成功") : ResponseResult.error("取消失败");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("取消失败：" + e.getMessage());
        }
    }

    @PostMapping("/certificate/check")
    public ResponseResult<?> checkCertificateIssued(
            @RequestBody Map<String, Object> request) {
        try {
            Long volunteerId = ((Number) request.get("volunteerId")).longValue();
            String certificateType = (String) request.get("certificateType");

            Integer assignmentId = null;
            if (request.get("assignmentId") != null) {
                assignmentId = ((Number) request.get("assignmentId")).intValue();
            }
            Long homeworkId = null;
            if (request.get("homeworkId") != null) {
                homeworkId = ((Number) request.get("homeworkId")).longValue();
            }

            boolean result = volunteerManageService.checkCertificateIssued(volunteerId, certificateType, assignmentId, homeworkId);
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.error("检查失败");
        }
    }

    @PostMapping("/certificate/list-by-homework")
    public ResponseResult<?> getCertificatesByHomeworkId(@RequestBody Map<String, Object> request) {
        try {
            Long homeworkId = ((Number) request.get("homeworkId")).longValue();
            List<Map<String, Object>> certificates = volunteerManageService.getCertificatesByHomeworkId(homeworkId);
            return ResponseResult.success(certificates);
        } catch (Exception e) {
            return ResponseResult.error("获取证书列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取志愿者详情
     */
    @GetMapping("/manage/volunteer-detail")
    public ResponseResult<Map<String, Object>> getVolunteerDetail(@RequestHeader("Authorization") String token,
                                                                  @RequestParam("volunteerId") Long volunteerId) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            Map<String, Object> volunteerDetail = volunteerManageService.getVolunteerDetail(volunteerId);
            if (!volunteerDetail.isEmpty()) {
                return ResponseResult.success(volunteerDetail);
            } else {
                return ResponseResult.error("志愿者不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取志愿者详情失败：" + e.getMessage());
        }
    }

    /**
     * 获取管理范围内的志愿者列表
     */
    @GetMapping("/manage/volunteers")
    public ResponseResult<List<Map<String, Object>>> getManagedVolunteers(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "assignmentId", required = false) Integer assignmentId) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            List<Map<String, Object>> volunteers = volunteerManageService.getManagedVolunteers(userId, assignmentId);
            return ResponseResult.success(volunteers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取志愿者列表失败");
        }
    }

    @PostMapping("/user/assignments")
    public ResponseResult<List<Map<String, Object>>> getUserAssignments(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        try {
            jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            Long userId = ((Number) request.get("userId")).longValue();
            List<Map<String, Object>> list = volunteerManageService.getUserAllAssignments(userId);
            return ResponseResult.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取岗位历史失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户是否为超级管理员
     */
    @GetMapping("/check-admin")
    public ResponseResult<Boolean> checkAdminPermission(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            boolean isAdmin = volunteerManageService.checkAdminPermission(userId);
            return ResponseResult.success(isAdmin);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("检查权限失败");
        }
    }

    /**
     * 获取正在进行的营期列表
     */
    @GetMapping("/admin/camps")
    public ResponseResult<List<Map<String, Object>>> getActiveCamps(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            boolean isAdmin = volunteerManageService.checkAdminPermission(userId);
            if (!isAdmin) {
                return ResponseResult.error("权限不足");
            }
            List<Map<String, Object>> camps = volunteerManageService.getActiveCamps();
            return ResponseResult.success(camps);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取营期列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取营期下的班级列表
     */
    @GetMapping("/admin/classes")
    public ResponseResult<List<Map<String, Object>>> getClassesByCampId(@RequestHeader("Authorization") String token,
                                                                        @RequestParam("campId") Integer campId) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            boolean isAdmin = volunteerManageService.checkAdminPermission(userId);
            if (!isAdmin) {
                return ResponseResult.error("权限不足");
            }
            List<Map<String, Object>> classes = volunteerManageService.getClassesByCampId(campId);
            return ResponseResult.success(classes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取班级列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取营期下的班长列表
     */
    @GetMapping("/admin/monitors")
    public ResponseResult<List<Map<String, Object>>> getMonitorsByCampId(@RequestHeader("Authorization") String token,
                                                                         @RequestParam("campId") Integer campId) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            boolean isAdmin = volunteerManageService.checkAdminPermission(userId);
            if (!isAdmin) {
                return ResponseResult.error("权限不足");
            }
            List<Map<String, Object>> monitors = volunteerManageService.getMonitorsByCampId(campId);
            return ResponseResult.success(monitors);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取班长列表失败：" + e.getMessage());
        }
    }

}