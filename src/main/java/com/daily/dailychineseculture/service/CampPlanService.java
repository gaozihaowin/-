package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.CampOptionDTO;
import com.daily.dailychineseculture.dto.CampPlanDTO;
import com.daily.dailychineseculture.dto.CampPlanSaveDayDTO;
import com.daily.dailychineseculture.dto.GenerateCalendarRequest;

import java.util.List;

/**
 * 排课计划 Service 接口
 */
public interface CampPlanService {

    /**
     * 获取营期下拉选项
     * @return 营期下拉选项列表
     */
    List<CampOptionDTO> getCampOptions();

    /**
     * 根据营期 ID 查询排课计划列表
     * 每个排课计划会包含其下的所有任务
     * @param campId 营期 ID
     * @return 排课计划列表
     */
    List<CampPlanDTO> getCampPlansByCampId(Integer campId);

    /**
     * 一键生成空日历
     * @param request 生成日历请求
     */
    void generateCalendar(GenerateCalendarRequest request);

    /**
     * 保存/更新单日课表
     * 包括更新排课基本信息和全量同步任务列表
     * @param campPlan 排课计划 DTO
     */
    void saveOrUpdateCampPlan(CampPlanDTO campPlan);

    /**
     * 新增一天的排课
     * @param campPlan 排课计划 DTO（包含 campId, dayIndex, planDate 等基本信息）
     * @return 新增后的排课计划（包含 planId）
     */
    CampPlanDTO addCampPlan(CampPlanDTO campPlan);

    /**
     * 删除整天排课及挂载的所有任务
     * @param planId 排课 ID
     */
    void deleteCampPlan(Integer planId);

    /**
     * 聚合保存单日排课（主表+任务列表全量刷新）
     * 采用全删全插策略：先删该日所有旧任务，再批量插入新任务
     * @param request 单日排课聚合保存请求
     */
    void saveDayPlan(CampPlanSaveDayDTO request);
}
