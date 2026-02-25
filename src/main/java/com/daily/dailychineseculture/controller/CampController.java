package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.CampVO;
import com.daily.dailychineseculture.entity.Camp;
import com.daily.dailychineseculture.service.CampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 营期控制器
 * 提供营期相关的API接口
 */
@RestController
@RequestMapping("/courses")
public class CampController {
    
    @Autowired
    private CampService campService;
    
    /**
     * 获取热门课程推荐列表
     * 接口路径: GET /courses/hot
     * 功能描述: 联表查询t_camp和t_camp_type，按开营时间倒序取最新的5条
     * 
     * @return 热门课程推荐列表
     */
    @GetMapping("/hot")
    public Result<List<CampVO>> getHotCourses() {
        try {
            List<CampVO> hotCourses = campService.getHotCourses();
            return Result.success(hotCourses);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取热门课程推荐失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有课程列表
     * 接口路径: GET /courses/all
     * 
     * @return 所有课程列表
     */
    @GetMapping("/all")
    public Result<List<Camp>> getAllCourses() {
        try {
            List<Camp> allCamps = campService.getAllCamps();
            return Result.success(allCamps);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取课程列表失败: " + e.getMessage());
        }
    }
}