package com.daily.dailychineseculture.vo;

import lombok.Data;
import java.util.Date;

@Data
public class RoleDetail {
    private Integer assignmentId;
    private String dutyType;
    private Date startTime;
    private Long campId;
    private String campName;
}
