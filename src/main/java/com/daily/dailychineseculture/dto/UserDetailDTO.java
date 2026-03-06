package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 用户个人资料详情 DTO
 * 用于 /user/detail 接口返回完整用户信息
 */
@Data
public class UserDetailDTO {
    
    /**
     * 账号（不允许修改）
     */
    private String account;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像 URL
     */
    private String avatar;
    
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
     * 生日 (格式化为 "yyyy-MM-dd")
     */
    private String birthday;
    
    /**
     * 密码字段（前端占位符，后端不返回真实密码，留空即可）
     */
    private String password;
}
