package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.dto.TaskItemDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlanTaskMapper {
    List<TaskItemDTO> selectTaskItemsByPlanIdAndUserId(@Param("planId") Integer planId, @Param("userId") Long userId);

    Integer countRequiredTasksByPlanId(@Param("planId") Integer planId);

    Integer countCompletedRequiredTasksByUserIdAndPlanId(@Param("userId") Long userId, @Param("planId") Integer planId);

    Integer countTaskInPlan(@Param("planId") Integer planId, @Param("taskId") Integer taskId);

    String selectTaskTypeByTaskId(@Param("taskId") Integer taskId);
}
