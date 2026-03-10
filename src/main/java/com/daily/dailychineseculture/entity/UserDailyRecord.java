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
    
    // 任务完成状态 (0 未完成，1 已完成)
    
    /**
     * 原文诵读：0 未完成，1 已完成
     */
    private Integer isReadDone;
    
    /**
     * 名师导读：0 未完成，1 已完成
     */
    private Integer isVideoDone;
    
    /**
     * 心得打卡：0 未完成，1 已完成
     */
    private Integer isHomeworkDone;
    
    /**
     * 备选任务 1:0 未完成，1 已完成
     */
    private Integer isExtra1Done;
    
    /**
     * 备选任务 2:0 未完成，1 已完成
     */
    private Integer isExtra2Done;
    
    // 总进度百分比 (0-100)
    
    /**
     * 完成率
     */
    private Integer completionRate;
}
