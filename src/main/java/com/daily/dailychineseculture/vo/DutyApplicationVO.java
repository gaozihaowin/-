package com.daily.dailychineseculture.vo;

import lombok.Data;

import java.util.Date;

/**
 * 权限申请列表返回VO
 */
@Data
public class DutyApplicationVO {

    /**
     * 申请ID
     */
    private Integer applyId;

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
     * 审核备注（审核通过/拒绝时的备注信息）
     */
    private String auditRemark;

    /**
     * 创建时间
     */
    private Date createTime;
}
