package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.CampOptionDTO;
import com.daily.dailychineseculture.dto.CampVO;
import com.daily.dailychineseculture.entity.Camp;
import com.daily.dailychineseculture.service.CampPlanService;
import com.daily.dailychineseculture.service.CampService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 营期控制器
 * 提供营期相关的 API 接口
 */
@RestController
@RequestMapping("/api/admin/camps")
@RequiredArgsConstructor
public class CampController {
    
    private final CampService campService;
    private final CampPlanService campPlanService;
    
    /**
     * 获取营期下拉选项
     * GET /api/admin/camps/options
     * 
     * @return 统一响应结果，包含营期下拉选项列表
     */
    @GetMapping("/options")
    public ResponseResult<List<CampOptionDTO>> getCampOptions() {
        List<CampOptionDTO> options = campPlanService.getCampOptions();
        return ResponseResult.success("查询成功", options);
    }
    
    /**
     * 获取热门课程推荐列表
     * 接口路径：GET /api/admin/camps/hot
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
     * 接口路径：GET /api/admin/camps/all
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