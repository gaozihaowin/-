package com.daily.dailychineseculture.entity;

import lombok.Data;
import java.util.Date;

/**
 * 用户每日学习记录实体类
 * 对应数据库表：t_user_daily_record
 */
@Data
public class UserDailyRecord {
    /**
     * 记录 ID (主键)
     */
    private Long recordId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 计划 ID
     */
    private Integer planId;

    /**
     * 学习日期
     */
    private Date date;

    /**
     * 当日学习时长（分钟）
     */
    private Integer learningDuration;

    /**
     * 签到状态：0 未签到，1 已签到
     */
    private Integer checkInStatus;

    /**
     * 固定任务 - 原文诵读：0 未完成，1 已完成
     */
    private Integer readStatus;

    /**
     * 固定任务 - 名师导读：0 未完成，1 已完成
     */
    private Integer videoStatus;

    /**
     * 固定任务 - 心得打卡：0 未完成，1 已完成
     */
    private Integer homeworkStatus;

    /**
     * 备选任务 1 名称
     */
    private String extraTask1Name;

    /**
     * 备选任务 1 状态：0 未完成，1 已完成
     */
    private Integer extraTask1Status;

    /**
     * 备选任务 2 名称
     */
    private String extraTask2Name;

    /**
     * 备选任务 2 状态：0 未完成，1 已完成
     */
    private Integer extraTask2Status;

    /**
     * 备选任务 3 名称
     */
    private String extraTask3Name;

    /**
     * 备选任务 3 状态：0 未完成，1 已完成
     */
    private Integer extraTask3Status;

    /**
     * 创建时间
     */
    private Date createTime;
}
