package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.CampOptionDTO;
import com.daily.dailychineseculture.dto.CampPlanDTO;
import com.daily.dailychineseculture.dto.GenerateCalendarRequest;
import com.daily.dailychineseculture.service.CampPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教务排课工作台 Controller
 */
@RestController
@RequestMapping("/api/admin/camp-plans")
@RequiredArgsConstructor
public class CampPlanController {
    
    private final CampPlanService campPlanService;
    
    /**
     * 获取某营期的排课时间轴
     * GET /api/admin/camp-plans?campId={campId}
     * 
     * @param campId 营期 ID
     * @return 统一响应结果，包含排课计划列表
     */
    @GetMapping
    public ResponseResult<List<CampPlanDTO>> getCampPlans(@RequestParam Integer campId) {
        List<CampPlanDTO> plans = campPlanService.getCampPlansByCampId(campId);
        return ResponseResult.success("查询成功", plans);
    }
    
    /**
     * 一键生成空日历
     * POST /api/admin/camp-plans/generate
     * 
     * @param request 生成日历请求
     * @return 统一响应结果
     */
    @PostMapping("/generate")
    public ResponseResult<String> generateCalendar(@RequestBody GenerateCalendarRequest request) {
        campPlanService.generateCalendar(request);
        return ResponseResult.success("日历框架生成成功");
    }
    
    /**
     * 保存/更新单日课表
     * PUT /api/admin/camp-plans
     * 
     * @param campPlan 排课计划 DTO
     * @return 统一响应结果
     */
    @PutMapping
    public ResponseResult<String> saveOrUpdateCampPlan(@RequestBody CampPlanDTO campPlan) {
        campPlanService.saveOrUpdateCampPlan(campPlan);
        return ResponseResult.success("保存成功");
    }
}
