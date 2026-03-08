package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.CampOptionDTO;
import com.daily.dailychineseculture.dto.CampPlanDTO;
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
     * @param campPlan 排课计划 DTO
     */
    void saveOrUpdateCampPlan(CampPlanDTO campPlan);
}
