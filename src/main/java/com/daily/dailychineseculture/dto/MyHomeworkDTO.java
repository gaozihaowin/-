package com.daily.dailychineseculture.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MyHomeworkDTO {
    private Integer homeworkId;
    private String campName;
    private String planTitle;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime submitTime;

    private Integer isSmallGroupExcellent;
    private Integer isBigGroupExcellent;
}
