package com.daily.dailychineseculture.entity;

import lombok.Data;

import java.util.Date;

/**
 * 权限申请实体类
 * 对应数据库表：t_duty_application
 */
@Data
public class DutyApplication {

    /**
     * 申请ID（主键自增）
     */
    private Integer applyId;

    /**
     * 申请人用户ID
     */
    private Long userId;

    /**
     * 营期ID（允许为 null，全局权限申请时为空）
     */
    private Integer campId;

    /**
     * 权限类型
     * COURSE_ADMIN(课程管理员), ARCHIVE_ADMIN(档案管理员), SUPER_ADMIN(超级管理员)
     */
    private String dutyType;

    /**
     * 申请理由
     */
    private String applyReason;

    /**
     * 审核状态
     * 0-待通过, 1-已通过, 2-未通过, 3-已撤销
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
