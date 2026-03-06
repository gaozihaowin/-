package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 用户资料更新请求 DTO
 * 用于接收 /user/updateAll 接口的 JSON 请求体
 */
@Data
public class UserUpdateAllRequest {
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像 URL
     */
    private String avatar;
    
    /**
     * 密码（如果为空字符串或 null，表示不修改密码）
     */
    private String password;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 地区
     */
    private String region;
    
    /**
     * 职业
     */
    private String profession;
    
    /**
     * 性别 (0:未知，1:男，2:女)
     */
    private Integer gender;
    
    /**
     * 生日 (格式："yyyy-MM-dd")
     */
    private String birthday;
}
