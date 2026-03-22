package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.CampOptionDTO;
import com.daily.dailychineseculture.dto.CampPlanDTO;
import com.daily.dailychineseculture.dto.GenerateCalendarRequest;
import com.daily.dailychineseculture.dto.PlanTaskDTO;
import com.daily.dailychineseculture.entity.Camp;
import com.daily.dailychineseculture.entity.CampPlan;
import com.daily.dailychineseculture.entity.PlanTask;
import com.daily.dailychineseculture.mapper.CampMapper;
import com.daily.dailychineseculture.mapper.CampPlanMapper;
import com.daily.dailychineseculture.mapper.PlanTaskMapper;
import com.daily.dailychineseculture.service.CampPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 排课计划 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class CampPlanServiceImpl implements CampPlanService {

    private final CampPlanMapper campPlanMapper;
    private final CampMapper campMapper;
    private final PlanTaskMapper planTaskMapper;

    @Override
    public List<CampOptionDTO> getCampOptions() {
        return campMapper.selectCampOptions();
    }

    /**
     * 根据营期 ID 查询排课计划列表
     * 每个排课计划会包含其下的所有任务
     */
    @Override
    public List<CampPlanDTO> getCampPlansByCampId(Integer campId) {
        // 1. 查询排课计划列表
        List<CampPlanDTO> plans = campPlanMapper.selectCampPlansByCampId(campId);

        // 2. 遍历每个排课计划，查询其下的所有任务
        for (CampPlanDTO plan : plans) {
            List<PlanTaskDTO> tasks = planTaskMapper.selectTasksByPlanId(plan.getPlanId());
            plan.setTasks(tasks);
        }

        return plans;
    }

    /**
     * 一键生成空日历
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateCalendar(GenerateCalendarRequest request) {
        Integer campId = request.getCampId();

        // 1. 校验：查询是否已存在排课计划
        int count = campPlanMapper.countCampPlansByCampId(campId);
        if (count > 0) {
            throw new RuntimeException("该营期已存在课表，请勿重复生成");
        }

        // 2. 查询营期信息
        Camp camp = campMapper.selectById(campId);
        if (camp == null) {
            throw new RuntimeException("未找到指定的营期");
        }

        // 3. 计算日期范围
        LocalDate startDate = convertToLocalDate(camp.getStartTime());
        LocalDate endDate = convertToLocalDate(camp.getEndTime());

        // 计算总天数（含起止日）
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (totalDays <= 0) {
            throw new RuntimeException("开营时间必须早于或等于结营时间");
        }

        // 4. 循环生成排课计划
        List<CampPlan> campPlans = new ArrayList<>();
        for (int i = 0; i < totalDays; i++) {
            CampPlan plan = new CampPlan();
            plan.setCampId(campId);
            plan.setDayIndex(i + 1);
            plan.setPlanDate(Date.from(startDate.plusDays(i).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            plan.setTitle("");

            campPlans.add(plan);
        }

        // 5. 批量插入
        campPlanMapper.batchInsertCampPlans(campPlans);
    }

    /**
     * 保存/更新单日课表
     * 包括更新排课基本信息和全量同步任务列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateCampPlan(CampPlanDTO campPlan) {
        Integer planId = campPlan.getPlanId();

        // 1. 更新排课基本信息
        campPlanMapper.updateCampPlan(campPlan);

        // 2. 全量同步任务列表
        if (campPlan.getTasks() != null) {
            syncTasks(planId, campPlan.getTasks());
        }
    }

    /**
     * 全量同步任务列表
     * 核心逻辑：
     * - 前端传了 taskId 且数据库有的，执行 update
     * - 前端传的 taskId 为 null 的，执行 insert
     * - 数据库里原有，但前端没传的 taskId，执行逻辑删除
     *
     * @param planId 排课 ID
     * @param newTasks 前端传来的任务列表
     */
    private void syncTasks(Integer planId, List<PlanTaskDTO> newTasks) {
        // 1. 查询数据库中该 planId 原有的所有任务 ID
        List<Integer> existingTaskIds = planTaskMapper.selectTaskIdsByPlanId(planId);
        Set<Integer> existingTaskIdSet = existingTaskIds.stream().collect(Collectors.toSet());

        // 2. 收集前端传来的有效 taskId（前端使用 Long，这里转 Integer）
        Set<Integer> newTaskIdSet = newTasks.stream()
                .map(dto -> dto.getTaskId() != null ? dto.getTaskId().intValue() : null)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 3. 找出需要删除的任务 ID（数据库有，前端没有）
        List<Integer> toDeleteTaskIds = existingTaskIds.stream()
                .filter(id -> !newTaskIdSet.contains(id))
                .collect(Collectors.toList());

        // 4. 批量逻辑删除不再需要的任务（is_deleted = 1）
        if (!toDeleteTaskIds.isEmpty()) {
            planTaskMapper.logicDeleteBatch(toDeleteTaskIds);
        }

        // 5. 遍历前端传来的任务，执行 insert 或 update
        for (PlanTaskDTO taskDTO : newTasks) {
            if (taskDTO.getTaskId() == null) {
                // 新增任务
                PlanTask newTask = convertToEntity(planId, taskDTO);
                planTaskMapper.insertTask(newTask);
                // 回填 taskId（如果前端需要）
                taskDTO.setTaskId(newTask.getTaskId());
            } else {
                // 更新任务
                PlanTask updateTask = convertToEntity(planId, taskDTO);
                planTaskMapper.updateTask(updateTask);
            }
        }
    }

    /**
     * 将 DTO 转换为实体
     */
    private PlanTask convertToEntity(Integer planId, PlanTaskDTO dto) {
        PlanTask task = new PlanTask();
        task.setTaskId(dto.getTaskId());
        task.setPlanId(planId);
        task.setTaskType(dto.getTaskType());
        task.setTaskName(dto.getTaskName());
        task.setTaskDesc(dto.getTaskDesc());
        task.setTaskUrl(dto.getTaskUrl());
        task.setDuration(dto.getDuration());
        task.setIsRequired(dto.getIsRequired());
        task.setSortOrder(dto.getSortOrder());
        return task;
    }

    /**
     * 新增一天的排课
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CampPlanDTO addCampPlan(CampPlanDTO campPlan) {
        // 1. 校验营期是否存在
        Camp camp = campMapper.selectById(campPlan.getCampId());
        if (camp == null) {
            throw new RuntimeException("未找到指定的营期");
        }

        // 2. 创建排课计划实体
        CampPlan plan = new CampPlan();
        plan.setCampId(campPlan.getCampId());
        plan.setDayIndex(campPlan.getDayIndex());
        plan.setPlanDate(campPlan.getPlanDate());
        plan.setTitle(campPlan.getTitle() != null ? campPlan.getTitle() : "");

        // 3. 插入排课计划
        campPlanMapper.insertCampPlan(plan);

        // 4. 如果有任务列表，同步插入任务
        if (campPlan.getTasks() != null && !campPlan.getTasks().isEmpty()) {
            for (PlanTaskDTO taskDTO : campPlan.getTasks()) {
                PlanTask newTask = convertToEntity(plan.getPlanId(), taskDTO);
                planTaskMapper.insertTask(newTask);
            }
            // 重新查询任务列表
            List<PlanTaskDTO> tasks = planTaskMapper.selectTasksByPlanId(plan.getPlanId());
            campPlan.setTasks(tasks);
        }

        // 5. 设置返回的 planId
        campPlan.setPlanId(plan.getPlanId());

        return campPlan;
    }

    /**
     * 删除整天排课
     * 直接删除 t_camp_plan 表中对应 ID 的记录即可
     * 数据库已有 ON DELETE CASCADE 约束自动清理底层任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCampPlan(Integer planId) {
        // 1. 校验排课是否存在
        CampPlan plan = campPlanMapper.selectById(planId);
        if (plan == null) {
            throw new RuntimeException("未找到指定的排课计划");
        }

        // 2. 直接删除排课计划，CASCADE 约束会自动清理底层任务
        campPlanMapper.deleteByPlanId(planId);
    }

    /**
     * 将 Date 转换为 LocalDate
     */
    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
