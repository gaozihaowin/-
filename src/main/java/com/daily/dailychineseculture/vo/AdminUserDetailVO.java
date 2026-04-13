package com.daily.dailychineseculture.vo;

import lombok.Data;
import java.util.List;

@Data
public class AdminUserDetailVO {
    private UserInfoVO userInfo;
    private List<ActiveRoleVO> activeRoles;
    private List<ApplicationHistoryVO> applicationHistory;
}
