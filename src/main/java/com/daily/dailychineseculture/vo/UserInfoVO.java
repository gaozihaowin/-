package com.daily.dailychineseculture.vo;

import lombok.Data;
import java.util.Date;

@Data
public class UserInfoVO {
    private Long userId;
    private String account;
    private String nickname;
    private String phone;
    private String avatar;
    private String region;
    private String profession;
    private Integer gender;
    private Date birthday;
    private Date createTime;
    private Integer status;
}
