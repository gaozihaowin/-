package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.List;

@Data
public class PromotionCheckResultDTO {
    private Long userId;
    private Integer currentCampId;
    private String currentCampName;
    private Integer currentTypeId;
    private String currentTypeName;
    private Integer nextTypeId;
    private String nextTypeName;
    private boolean eligible;
    private String reason;
    private Integer progress;
    private Integer totalPlans;
    private Integer submittedHomework;
    private Integer missedConsecutive;
    private Integer lateSubmissions;
    private Integer missedSubmissions;
    private List<NextCampDTO> availableCamps;

    @Data
    public static class NextCampDTO {
        private Integer campId;
        private String name;
        private String term;
    }
}
