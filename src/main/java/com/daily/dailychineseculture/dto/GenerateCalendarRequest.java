package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 生成日历请求 DTO
 */
@Data
public class GenerateCalendarRequest {
    /**
     * 营期 ID
     */
    private Integer campId;
}
