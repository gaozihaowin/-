package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.List;

/**
 * 分班结果DTO
 */
@Data
public class ClassAssignResultDTO {

    /**
     * 营期ID
     */
    private Integer campId;

    /**
     * 总学员数
     */
    private Integer totalStudents;

    /**
     * 分班数量
     */
    private Integer classCount;

    /**
     * 每班平均人数
     */
    private Integer avgPerClass;

    /**
     * 班级详情列表
     */
    private List<ClassDetail> classes;

    @Data
    public static class ClassDetail {
        private Integer classId;
        private String className;
        private Integer studentCount;
        private List<StudentInfo> students;
    }

    @Data
    public static class StudentInfo {
        private Long userId;
        private String nickname;
        private String region;
        private Integer gender;
    }
}