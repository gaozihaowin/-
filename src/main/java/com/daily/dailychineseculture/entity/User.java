package com.daily.dailychineseculture.entity;

import lombok.Data;
import java.util.Date;

/**
 * 用户实体类
 */
@Data
public class User {
    /**
     * 用户ID (由Java端计算生成: YYYY+000000)
     */
    private Long userId;
    
    /**
     * 账户名
     */
    private String account;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 地域
     */
    private String region;
    
    /**
     * 生日
     */
    private Date birthday;
    
    /**
     * 职业
     */
    private String profession;
    
    /**
     * 性别: 0未知, 1男, 2女
     */
    private Integer gender;
    
    /**
     * 注册时间
     */
    private Date createTime;
    
    /**
     * 状态: 1正常, 0冻结
     */
    private Integer status;
}