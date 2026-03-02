package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.List;

/**
 * 志愿者统计DTO
 */
@Data
public class VolunteerStatsDTO {
    /**
     * 参与的营期列表
     */
    private List<CampItem> enrollCamps;

    /**
     * 负责的班级列表
     */
    private List<ClassItem> dutyClasses;

    /**
     * 负责的大组列表
     */
    private List<BigGroupItem> dutyBigGroups;

    /**
     * 负责的小组列表
     */
    private List<SmallGroupItem> dutySmallGroups;

    @Data
    public static class CampItem {
        private Integer campId;
        private String campName;
    }

    @Data
    public static class ClassItem {
        private Integer campId;
        private String campName;
        private Integer classId;
        private String className;
    }

    @Data
    public static class BigGroupItem {
        private Integer campId;
        private String campName;
        private Integer classId;
        private String className;
        private Integer bigGroupId;
        private String bigGroupName;
    }

    @Data
    public static class SmallGroupItem {
        private Integer campId;
        private String campName;
        private Integer classId;
        private String className;
        private Integer bigGroupId;
        private String bigGroupName;
        private Integer smallGroupId;
        private String smallGroupName;
    }
}