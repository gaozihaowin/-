package com.daily.dailychineseculture.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Homework {
    private Integer homeworkId;
    private Long userId;
    private Integer planId;
    private Integer taskId;
    private String content;
    private LocalDateTime submitTime;
    private Integer isSmallGroupExcellent;
    private Integer isBigGroupExcellent;
}
