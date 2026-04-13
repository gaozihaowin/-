package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.ClassAssignResultDTO;
import com.daily.dailychineseculture.dto.ClassDTO;
import com.daily.dailychineseculture.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端分班控制器
 * 提供新生分班相关的管理API接口
 */
@RestController
@RequestMapping("/api/admin/class")
public class AdminClassController {

    @Autowired
    private ClassService classService;

    @GetMapping("/unassigned-students")
    public ResponseResult<List<Map<String, Object>>> getUnassignedStudents(@RequestParam Integer campId) {
        try {
            List<Map<String, Object>> students = classService.getUnassignedStudents(campId);
            return ResponseResult.success("查询成功", students);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取待分班学员失败: " + e.getMessage());
        }
    }

    @GetMapping("/camp-classes")
    public ResponseResult<List<ClassDTO>> getCampClasses(@RequestParam Integer campId) {
        try {
            List<ClassDTO> classes = classService.getClassesByCampId(campId);
            return ResponseResult.success("查询成功", classes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取班级列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/assign-by-count")
    public ResponseResult<ClassAssignResultDTO> assignStudentsByCount(@RequestBody ClassAssignResultDTO request) {
        try {
            if (request.getCampId() == null || request.getCampId() <= 0) {
                return ResponseResult.error(400, "营期ID无效");
            }
            if (request.getClassCount() == null || request.getClassCount() <= 0) {
                return ResponseResult.error(400, "班级数量必须大于0");
            }
            return classService.assignStudentsByClassCount(request.getCampId(), request.getClassCount());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("分班失败: " + e.getMessage());
        }
    }

    @GetMapping("/students")
    public ResponseResult<List<Map<String, Object>>> getAllStudents(@RequestParam Integer campId) {
        try {
            List<Map<String, Object>> students = classService.getAllStudentsByCampId(campId);
            return ResponseResult.success("查询成功", students);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取学员列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/move-student")
    public ResponseResult<Void> moveStudentToClass(@RequestBody Map<String, Object> request) {
        try {
            Long userId = ((Number) request.get("userId")).longValue();
            Integer campId = ((Number) request.get("campId")).intValue();
            Integer newClassId = request.get("newClassId") != null ? ((Number) request.get("newClassId")).intValue() : null;
            return classService.moveStudentToClass(userId, campId, newClassId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("移动学员失败: " + e.getMessage());
        }
    }

    @GetMapping("/student-detail")
    public ResponseResult<Map<String, Object>> getStudentDetail(@RequestParam Long userId, @RequestParam Integer campId) {
        try {
            Map<String, Object> detail = classService.getStudentDetail(userId, campId);
            if (detail == null) {
                return ResponseResult.error("学员不存在或不属于该营期");
            }
            return ResponseResult.success("查询成功", detail);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取学员详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/reset-assignment")
    public ResponseResult<Void> resetAssignment(@RequestParam Integer campId) {
        return classService.resetAssignment(campId);
    }
}