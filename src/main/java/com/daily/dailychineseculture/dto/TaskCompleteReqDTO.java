package com.daily.dailychineseculture.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务完成请求 DTO
 * 用于微信小程序端打卡接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompleteReqDTO {
    private Integer taskId;
}
