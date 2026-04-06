package com.daily.dailychineseculture.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class SuperAdminDashboardVO {
    private Integer totalUsers;
    private Integer activeCamps;
    private Integer activeAdmins;
    private Integer pendingApprovalsCount;
    private List<ApprovalItemVO> latestPendingApprovals;

    @Data
    public static class ApprovalItemVO {
        private Integer applyId;
        private String applicantName;
        private String dutyType;
        private Date createTime;
    }
}
