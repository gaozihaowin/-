package com.daily.dailychineseculture.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class CampOptionDTO {
    private Integer campId;

    private String name;

    private Integer term;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private java.time.LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private java.time.LocalDateTime endTime;

    private Integer campStatusCode;

    private String campStatusDesc;
}
