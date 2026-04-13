package com.daily.dailychineseculture.vo;

import lombok.Data;

import java.util.Date;

/**
 * 审批列表单项VO
 */
@Data
public class AdminDutyApplicationListItemVO {

    /**
     * 申请ID
     */
    private Integer applyId;

    /**
     * 申请人用户ID
     */
    private Long userId;

    /**
     * 申请人姓名（从用户表JOIN出来）
     */
    private String applicantName;

    /**
     * 权限类型
     */
    private String dutyType;

    /**
     * 申请理由
     */
    private String applyReason;

    /**
     * 审核状态：0-待通过, 1-已通过, 2-未通过, 3-已撤销
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;
}
