package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.Date;

@Data
public class CampEnrollmentDTO {
    private Integer enrollId;
    private Long userId;
    private Integer campId;
    private String nickname;
    private Integer gender;
    private String region;
    private String birthday;
    private Integer classId;
    private String className;
    private Integer bigGroupId;
    private String bigGroupName;
    private Integer smallGroupId;
    private String smallGroupName;
    private Integer isCompleted;
    private Integer progress;
    private Date createTime;
}