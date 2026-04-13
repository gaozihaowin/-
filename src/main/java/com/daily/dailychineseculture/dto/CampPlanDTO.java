package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 排课计划 DTO
 * 用于管理后台排课数据的传输
 */
@Data
public class CampPlanDTO {
    /**
     * 计划 ID
     */
    private Integer planId;

    /**
     * 营期 ID
     */
    private Integer campId;

    /**
     * 第几天
     */
    private Integer dayIndex;

    /**
     * 具体日期
     */
    private Date planDate;

    /**
     * 导读标题
     */
    private String title;

    /**
     * 模块名称（如"第二周：知行合一"）
     * 用于前端周主题智能推算
     */
    private String moduleName;

    /**
     * 模块索引（第几周）
     * 用于前端周主题智能推算
     */
    private Integer moduleIndex;

    /**
     * 讲师姓名
     * 用于前端显示授课老师
     */
    private String teacherName;

    /**
     * 任务列表
     * 该排课下的所有任务（视频、阅读、作业等）
     */
    private List<PlanTaskDTO> tasks;
}
