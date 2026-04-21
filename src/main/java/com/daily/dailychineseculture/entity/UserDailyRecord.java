package com.daily.dailychineseculture.entity;

import lombok.Data;

/**
 * 用户每日学习记录实体类
 * 对应数据库表：t_user_daily_record
 */
@Data
public class UserDailyRecord {
    /**
     * 记录 ID (主键)
     */
    private Integer recordId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 营期 ID
     */
    private Integer campId;

    /**
     * 计划 ID
     */
    private Integer planId;

    /**
     * 完成率 (0-100)
     */
    private Integer completionRate;

    /**
     * 是否全部完成：0-未完成，1-已完成
     */
    private Integer isAllCompleted;
}
