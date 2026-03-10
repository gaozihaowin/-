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
     * 记录ID (主键)
     */
    private Long recordId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 学习日期
     */
    private Date date;

    /**
     * 当日学习时长（分钟）
     */
    private Integer learningDuration;

    /**
     * 签到状态: 0未签到, 1已签到
     */
    private Integer checkInStatus;

    /**
     * 创建时间
     */
    private Date createTime;
}