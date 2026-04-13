package com.daily.dailychineseculture.vo;

import lombok.Data;
import java.util.Date;

@Data
public class ApplicationHistoryVO {
    private Integer applyId;
    private String dutyType;
    private String applyReason;
    private Integer status;
    private Date createTime;
    private Date reviewTime;
    private String auditRemark;
}
