package com.daily.dailychineseculture.vo;

import lombok.Data;
import java.util.Date;

@Data
public class ActiveRoleVO {
    private Integer assignmentId;
    private String dutyType;
    private Date startTime;
    private String campName;
}
