package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 作业详情DTO
 */
@Data
public class HomeworkDetailDTO {
    /**
     * 作业ID
     */
    private Integer homeworkId;

    /**
     * 学生昵称
     */
    private String studentName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 所属组织
     */
    private String organization;

    /**
     * 提交时间
     */
    private String submitTime;

    /**
     * 是否优秀作业
     */
    private Boolean isExcellent;

    /**
     * 作业内容
     */
    private String content;

    public HomeworkDetailDTO() {}

    public HomeworkDetailDTO(Integer homeworkId, String studentName, Long userId, String organization,
                             String submitTime, Boolean isExcellent, String content) {
        this.homeworkId = homeworkId;
        this.studentName = studentName;
        this.userId = userId;
        this.organization = organization;
        this.submitTime = submitTime;
        this.isExcellent = isExcellent;
        this.content = content;
    }
}