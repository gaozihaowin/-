package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.dto.CampPlanDTO;
import com.daily.dailychineseculture.entity.CampPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 排课计划 Mapper 接口
 */
@Mapper
public interface CampPlanMapper {
    
    /**
     * 根据营期 ID 查询排课计划列表
     * @param campId 营期 ID
     * @return 排课计划列表
     */
    List<CampPlanDTO> selectCampPlansByCampId(@Param("campId") Integer campId);
    
    /**
     * 检查营期是否已有排课计划
     * @param campId 营期 ID
     * @return 记录数
     */
    int countCampPlansByCampId(@Param("campId") Integer campId);
    
    /**
     * 批量插入排课计划
     * @param campPlans 排课计划列表
     * @return 影响行数
     */
    int batchInsertCampPlans(@Param("list") List<CampPlan> campPlans);
    
    /**
     * 根据 ID 更新排课计划
     * @param campPlan 排课计划 DTO
     * @return 影响行数
     */
    int updateCampPlan(CampPlanDTO campPlan);
}
