package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.List;

/**
 * 志愿者历史记录DTO
 */
@Data
public class VolunteerHistoryDTO {
    private List<VolunteerHistoryItem> volunteerHistory;

    @Data
    public static class VolunteerHistoryItem {
        /**
         * 职责任命ID
         */
        private Integer assignmentId;

        /**
         * 负责范围（营期-班级-大组-小组）
         */
        private String responsible;

        /**
         * 职责名称
         */
        private String duty;

        /**
         * 服务时间（开始时间-结束时间）
         */
        private String serviceTime;

        /**
         * 状态：正在参与/已结束
         */
        private String status;

        public VolunteerHistoryItem() {}

        public VolunteerHistoryItem(Integer assignmentId, String responsible, String duty,
                                    String serviceTime, String status) {
            this.assignmentId = assignmentId;
            this.responsible = responsible;
            this.duty = duty;
            this.serviceTime = serviceTime;
            this.status = status;
        }
    }
}