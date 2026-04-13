package com.daily.dailychineseculture.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 任务实体类
 * 对应数据库表：t_plan_task
 */
@Data
@Alias("PlanTask")
public class PlanTask {
    /**
     * 任务 ID
     */
    private Integer taskId;

    /**
     * 排课计划 ID
     */
    private Integer planId;

    /**
     * 任务类型
     * VIDEO - 视频
     * READ - 阅读
     * HOMEWORK - 作业
     */
    private String taskType;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务描述
     */
    private String taskDesc;

    /**
     * 任务链接
     */
    private String taskUrl;

    /**
     * 关联课件ID
     */
    private Long materialId;

    /**
     * 建议时长（分钟）
     */
    private Integer duration;

    /**
     * 是否必做
     * 1 - 必修
     * 0 - 选修
     */
    private Integer isRequired;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 逻辑删除标记
     * 0 - 正常
     * 1 - 已删除
     */
    private Integer isDeleted;
}
