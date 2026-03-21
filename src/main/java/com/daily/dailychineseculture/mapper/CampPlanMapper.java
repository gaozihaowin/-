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
     * 插入单条排课计划
     * @param campPlan 排课计划实体
     * @return 影响行数
     */
    int insertCampPlan(CampPlan campPlan);

    /**
     * 根据 ID 更新排课计划
     * @param campPlan 排课计划 DTO
     * @return 影响行数
     */
    int updateCampPlan(CampPlanDTO campPlan);

    /**
     * 根据 ID 删除排课计划
     * @param planId 计划 ID
     * @return 影响行数
     */
    int deleteByPlanId(@Param("planId") Integer planId);

    /**
     * 根据营期 ID 查询课程安排（按天排序）
     * @param campId 营期 ID
     * @return 课程安排列表
     */
    List<CampPlan> selectCourseScheduleByCampId(@Param("campId") Integer campId);

    /**
     * 查询指定日期的排课计划
     * @param campId 营期 ID
     * @param planDate 计划日期
     * @return 排课计划
     */
    CampPlan selectTodayPlan(@Param("campId") Integer campId, @Param("planDate") java.util.Date planDate);

    /**
     * 根据 ID 查询排课计划
     * @param planId 计划 ID
     * @return 排课计划
     */
    CampPlan selectById(@Param("planId") Integer planId);

    /**
     * 查询营期已发生的最近 7 节课（plan_date <= today，按 day_index 降序）
     * @param campId 营期 ID
     * @param today 当前日期
     * @return 最近课程列表
     */
    List<CampPlan> selectRecentPlansByCampId(@Param("campId") Integer campId, @Param("today") java.util.Date today);
}
