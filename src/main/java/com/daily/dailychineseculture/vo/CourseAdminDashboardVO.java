package com.daily.dailychineseculture.vo;

import lombok.Data;
import java.util.List;

@Data
public class CourseAdminDashboardVO {
    private Integer totalCamps;
    private Integer activeCamps;
    private Integer activeStudents;
    private Integer pendingApprovals;
    private List<RecentCampVO> recentCamps;

    @Data
    public static class RecentCampVO {
        private Integer campId;
        private String campName;
        private String campType;
        private Integer planCount;
        private String status;
        private Integer enrollCount;
    }
}
