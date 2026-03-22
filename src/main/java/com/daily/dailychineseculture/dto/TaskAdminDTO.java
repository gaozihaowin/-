package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 任务管理 DTO
 * 用于管理后台排课时的任务数据传输
 */
@Data
public class TaskAdminDTO {
    /**
     * 任务 ID
     * 如果是前端新建的任务，此字段为 null
     */
    private Integer taskId;

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
     * 作业的描述或阅读的说明存这里
     */
    private String taskDesc;

    /**
     * 任务链接
     * 视频或阅读的链接
     */
    private String taskUrl;

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
}
