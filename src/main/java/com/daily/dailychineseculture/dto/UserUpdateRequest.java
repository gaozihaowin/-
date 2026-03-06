package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.Date;

/**
 * 用户信息更新请求 DTO
 */
@Data
public class UserUpdateRequest {

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别：0 未知，1 男，2 女
     */
    private Integer gender;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 地域
     */
    private String region;

    /**
     * 职业
     */
    private String profession;
}
