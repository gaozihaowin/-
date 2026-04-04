package com.daily.dailychineseculture.dto;

import lombok.Data;

import java.util.Date;

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
    private Date submitTime;

    /**
     * 是否小组优秀作业
     */
    private Integer isSmallGroupExcellent; // 0否，1是

    /**
     * 是否大组优秀作业
     */
    private Integer isBigGroupExcellent;   // 0否，1是

    /**
     * 作业内容
     */
    private String content;

    public HomeworkDetailDTO() {}

    public HomeworkDetailDTO(Integer homeworkId, String studentName, Long userId, String organization,
                             Date submitTime, Integer isSmallGroupExcellent, Integer isBigGroupExcellent, String content) {
        this.homeworkId = homeworkId;
        this.studentName = studentName;
        this.userId = userId;
        this.organization = organization;
        this.submitTime = submitTime;
        this.isSmallGroupExcellent = isSmallGroupExcellent;
        this.isBigGroupExcellent = isBigGroupExcellent;
        this.content = content;
    }
}