package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.List;

/**
 * 用户个人信息响应 DTO
 * 用于 /user/info 接口返回
 */
@Data
public class UserProfileDTO {
    
    /**
     * 用户 ID
     */
    private String userId;
    
    /**
     * 账号
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
     * 当前身份（学员端/志愿者端）
     */
    private String currentIdentity;
    
    /**
     * 统计指标列表（地区、职业、年数、学时）
     */
    private List<UserStatsItem> statsList;
}
