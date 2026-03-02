package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.ClassDTO;
import com.daily.dailychineseculture.dto.BigGroupDTO;
import com.daily.dailychineseculture.dto.SmallGroupDTO;
import com.daily.dailychineseculture.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 班级、大组、小组控制器
 * 提供组织架构相关的API接口
 */
@RestController
@RequestMapping("")
public class ClassController {

    @Autowired
    private ClassService classService;

    /**
     * 获取所有班级列表
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
     * 获取所有大组列表
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
     * 获取所有小组列表
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
}