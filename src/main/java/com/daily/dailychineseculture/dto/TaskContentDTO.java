package com.daily.dailychineseculture.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskContentDTO {

    private Integer taskId;

    private String taskType;

    private String title;

    private String subtitle;

    private Integer isRequired;

    private Integer isDone;

    private String taskUrl;
}
