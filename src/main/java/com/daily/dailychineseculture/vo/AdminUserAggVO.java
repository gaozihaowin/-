package com.daily.dailychineseculture.vo;

import lombok.Data;
import java.util.List;

@Data
public class AdminUserAggVO {
    private Long userId;
    private String nickname;
    private String phone;
    private String avatar;
    private List<RoleDetail> roles;
}
