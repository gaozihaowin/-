package com.daily.dailychineseculture.vo;

import lombok.Data;
import java.util.Date;

@Data
public class SystemAdminVO {
    private Integer assignmentId;
    private Long userId;
    private String phone;
    private String nickname;
    private String avatar;
    private String dutyType;
    private Date startTime;
}
