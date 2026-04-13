package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.dto.PlanTaskDTO;
import com.daily.dailychineseculture.dto.TaskItemDTO;
import com.daily.dailychineseculture.entity.PlanTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务 Mapper 接口
 */
@Mapper
public interface PlanTaskMapper {

    /**
     * 根据排课 ID 和用户 ID 查询任务列表（C 端使用）
     * @param planId 排课 ID
     * @param userId 用户 ID
     * @return 任务列表
     */
    List<TaskItemDTO> selectTaskItemsByPlanIdAndUserId(@Param("planId") Integer planId, @Param("userId") Long userId);

    /**
     * 统计某排课下的必做任务数量
     * @param planId 排课 ID
     * @return 必做任务数量
     */
    Integer countRequiredTasksByPlanId(@Param("planId") Integer planId);

    /**
     * 统计用户在某排课下已完成的必做任务数量
     * @param userId 用户 ID
     * @param planId 排课 ID
     * @return 已完成的必做任务数量
     */
    Integer countCompletedRequiredTasksByUserIdAndPlanId(@Param("userId") Long userId, @Param("planId") Integer planId);

    /**
     * 检查任务是否属于某排课
     * @param planId 排课 ID
     * @param taskId 任务 ID
     * @return 数量
     */
    Integer countTaskInPlan(@Param("planId") Integer planId, @Param("taskId") Integer taskId);

    /**
     * 根据任务 ID 查询任务类型
     * @param taskId 任务 ID
     * @return 任务类型
     */
    String selectTaskTypeByTaskId(@Param("taskId") Integer taskId);

    /**
     * 根据排课 ID 查询所有任务（管理后台使用）
     * @param planId 排课 ID
     * @return 任务列表
     */
    List<PlanTaskDTO> selectTasksByPlanId(@Param("planId") Integer planId);

    /**
     * 根据排课 ID 查询所有任务 ID
     * @param planId 排课 ID
     * @return 任务 ID 列表
     */
    List<Integer> selectTaskIdsByPlanId(@Param("planId") Integer planId);

    /**
     * 插入新任务
     * @param task 任务实体
     * @return 影响行数
     */
    Integer insertTask(PlanTask task);

    /**
     * 更新任务
     * @param task 任务实体
     * @return 影响行数
     */
    Integer updateTask(PlanTask task);

    /**
     * 根据任务 ID 删除任务（物理删除）
     * @param taskId 任务 ID
     * @return 影响行数
     */
    Integer deleteByTaskId(@Param("taskId") Integer taskId);

    /**
     * 根据排课 ID 删除所有任务（物理删除）
     * @param planId 排课 ID
     * @return 影响行数
     */
    Integer deleteByPlanId(@Param("planId") Integer planId);

    /**
     * 根据排课 ID 删除所有任务（物理删除）
     * @param planId 排课 ID
     * @return 影响行数
     */
    Integer deleteTasksByPlanId(@Param("planId") Integer planId);

    /**
     * 批量删除任务（物理删除）
     * @param taskIds 任务 ID 列表
     * @return 影响行数
     */
    Integer deleteByTaskIds(@Param("taskIds") List<Integer> taskIds);

    /**
     * 批量逻辑删除任务
     * @param taskIds 任务 ID 列表
     * @return 影响行数
     */
    Integer logicDeleteBatch(@Param("taskIds") List<Integer> taskIds);

    /**
     * 批量插入任务
     * @param tasks 任务列表
     * @return 影响行数
     */
    Integer batchInsertTasks(@Param("tasks") List<PlanTask> tasks);

    /**
     * 根据任务ID列表批量删除任务（物理删除）
     * @param ids 任务ID列表
     */
    void deleteTasksByIds(@Param("ids") List<Integer> ids);

    /**
     * 批量更新任务
     * @param tasks 任务列表
     */
    void batchUpdateTasks(@Param("tasks") List<PlanTask> tasks);

    /**
     * 根据素材ID统计排课任务数量（防爆拦截）
     * @param materialId 素材ID
     * @return 被引用数量
     */
    Integer countByMaterialId(@Param("materialId") Long materialId);

    /**
     * 根据素材ID更新排课任务的task_url（同步更新）
     * @param materialId 素材ID
     * @param newUrl 新的URL
     * @return 影响行数
     */
    Integer updateTaskUrlByMaterialId(@Param("materialId") Long materialId, @Param("newUrl") String newUrl);
}
