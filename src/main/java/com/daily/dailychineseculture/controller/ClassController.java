package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.ClassDTO;
import com.daily.dailychineseculture.dto.BigGroupDTO;
import com.daily.dailychineseculture.dto.SmallGroupDTO;
import com.daily.dailychineseculture.entity.User;
import com.daily.dailychineseculture.service.ClassService;
import com.daily.dailychineseculture.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 班级、大组、小组控制器
 * 提供组织架构相关的API接口（包含分班+分组）
 */
@RestController
@RequestMapping("")
public class ClassController {

    @Autowired
    private ClassService classService;

    // 注入UserService处理分班逻辑
    @Autowired
    private UserService userService;

    /**
     * 获取所有班级列表（队友的分组逻辑，不修改）
     * 接口路径: GET /class/list
     * 功能描述: 获取系统中所有班级的信息，包含所属营期
     *
     * @return 班级列表
     */
    @GetMapping("/class/list")
    public Result<List<ClassDTO>> getClassList() {
        try {
            List<ClassDTO> classList = classService.getAllClasses();
            return Result.success(classList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取班级列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有大组列表（队友的分组逻辑，不修改）
     * 接口路径: GET /bigGroup/list
     * 功能描述: 获取系统中所有大组的信息，包含所属班级和营期
     *
     * @return 大组列表
     */
    @GetMapping("/bigGroup/list")
    public Result<List<BigGroupDTO>> getBigGroupList() {
        try {
            List<BigGroupDTO> bigGroupList = classService.getAllBigGroups();
            return Result.success(bigGroupList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取大组列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有小组列表（队友的分组逻辑，不修改）
     * 接口路径: GET /smallGroup/list
     * 功能描述: 获取系统中所有小组的信息，包含所属大组、班级和营期
     *
     * @return 小组列表
     */
    @GetMapping("/smallGroup/list")
    public Result<List<SmallGroupDTO>> getSmallGroupList() {
        try {
            List<SmallGroupDTO> smallGroupList = classService.getAllSmallGroups();
            return Result.success(smallGroupList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取小组列表失败: " + e.getMessage());
        }
    }

    // ========== 新增的分班相关接口（适配Result类规范） ==========

    /**
     * 查询指定营期下审核通过的未分班学员
     * 接口路径: GET /class/auditPassStudents
     * 功能描述: 获取指定营期内待分班的学员列表
     *
     * @param campId 营期/课程ID（必传）
     * @return 待分班学员列表
     */
    @GetMapping("/class/auditPassStudents")
    public Result<List<User>> getAuditPassStudents(@RequestParam Long campId) {
        try {
            List<User> students = userService.getAuditPassStudents(campId);
            return Result.success(students); // 仅传数据，符合Result类规范
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取待分班学员列表失败: " + e.getMessage());
        }
    }

    /**
     * 执行自动分班操作
     * 接口路径: GET /class/autoAssign
     * 功能描述: 对指定营期的待分班学员按地域优先+年龄平衡规则自动分班
     *
     * @param campId 营期/课程ID（必传）
     * @param perClassNum 每班人数上限（必传，建议值：10-20）
     * @return 分班后的学员列表
     */
    @GetMapping("/class/autoAssign")
    public Result<List<User>> autoAssignClass(
            @RequestParam Long campId,
            @RequestParam Integer perClassNum) {
        try {
            // 参数校验：避免非法参数
            if (perClassNum <= 0) {
                return Result.error("每班人数上限必须大于0");
            }

            List<User> students = userService.autoAssignClass(campId, perClassNum);
            return Result.success(students); // 仅传数据，符合Result类规范
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("自动分班失败: " + e.getMessage());
        }
    }
}