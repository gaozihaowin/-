package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.AppendCampPlanRequest;
import com.daily.dailychineseculture.dto.CampOptionDTO;
import com.daily.dailychineseculture.dto.CampPlanAddDayDTO;
import com.daily.dailychineseculture.dto.CampPlanDTO;
import com.daily.dailychineseculture.dto.CampPlanSaveDayDTO;
import com.daily.dailychineseculture.dto.GenerateCalendarRequest;
import com.daily.dailychineseculture.service.CampPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教务排课工作台 Controller
 * 支持排课计划的 CRUD 操作，以及任务的一对多管理
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
     * 每个排课计划会包含其下的所有任务列表
     *
     * @param campId 营期 ID
     * @return 统一响应结果，包含排课计划列表（含任务）
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
     * 新增一天的排课
     * POST /api/admin/camp-plans
     *
     * @param campPlan 排课计划 DTO（包含 campId, dayIndex, planDate 等基本信息）
     * @return 统一响应结果，包含新增后的排课计划（含 planId）
     */
    @PostMapping
    public ResponseResult<CampPlanDTO> addCampPlan(@RequestBody CampPlanDTO campPlan) {
        CampPlanDTO result = campPlanService.addCampPlan(campPlan);
        return ResponseResult.success("新增成功", result);
    }

    /**
     * 保存/更新单日课表
     * PUT /api/admin/camp-plans
     *
     * 包括更新排课基本信息和全量同步任务列表
     *
     * @param campPlan 排课计划 DTO（包含 planId, title, tasks 等）
     * @return 统一响应结果
     */
    @PutMapping
    public ResponseResult<String> saveOrUpdateCampPlan(@RequestBody CampPlanDTO campPlan) {
        campPlanService.saveOrUpdateCampPlan(campPlan);
        return ResponseResult.success("保存成功");
    }

    /**
     * 删除整天排课及挂载的所有任务
     * DELETE /api/admin/camp-plans/{planId}
     *
     * @param planId 排课 ID
     * @return 统一响应结果
     */
    @DeleteMapping("/{planId}")
    public ResponseResult<String> deleteCampPlan(@PathVariable Integer planId) {
        campPlanService.deleteCampPlan(planId);
        return ResponseResult.success("删除成功");
    }

    @PutMapping("/save-day")
    public ResponseResult<String> saveDay(@Valid @RequestBody CampPlanSaveDayDTO request) {
        campPlanService.saveDayPlan(request);
        return ResponseResult.success("保存成功");
    }

    /**
     * 追加一天排课
     * POST /api/admin/camp-plans/append
     *
     * @param request 请求体，包含 campId
     * @return 统一响应结果，包含新增的排课计划（含 planId）
     */
    @PostMapping("/append")
    public ResponseResult<CampPlanDTO> appendDay(@RequestBody AppendCampPlanRequest request) {
        CampPlanDTO result = campPlanService.appendDay(request.getCampId());
        return ResponseResult.success("追加成功", result);
    }

    /**
     * 智能追加一天排课
     * POST /api/admin/camp-plans/add-smart-day
     *
     * 前端智能推算完整数据后，后端仅负责落库
     *
     * @param requestDTO 智能追加排课请求 DTO
     * @return 统一响应结果
     */
    @PostMapping("/add-smart-day")
    public ResponseResult<Void> addSmartDay(@RequestBody @Validated CampPlanAddDayDTO requestDTO) {
        campPlanService.addSmartDay(requestDTO);
        return ResponseResult.successMsg("智能追加排课成功");
    }
}
