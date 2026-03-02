package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 切换身份请求参数
 */
@Data
public class SwitchIdentityRequest {
    /**
     * 身份类型：学员端/志愿者端
     */
    private String identity;
}