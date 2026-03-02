package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 微信登录请求参数
 */
@Data
public class WxLoginRequest {
    /**
     * 微信授权码
     */
    private String code;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;
}