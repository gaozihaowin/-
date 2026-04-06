package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.AssignRequest;
import com.daily.dailychineseculture.dto.RevokeRequest;
import com.daily.dailychineseculture.vo.AdminStatsVO;
import com.daily.dailychineseculture.vo.AdminUserAggVO;
import com.daily.dailychineseculture.vo.AdminUserDetailVO;
import com.daily.dailychineseculture.vo.SystemAdminVO;
import com.daily.dailychineseculture.vo.UserSearchVO;
import java.util.List;

public interface SystemAdminService {

    AdminStatsVO getAdminStats();

    List<SystemAdminVO> getAdminList(String keyword, Integer page, Integer pageSize);

    List<AdminUserAggVO> getAdminListAgg(String keyword);

    AdminUserDetailVO getAdminDetail(Long userId);

    List<UserSearchVO> searchUsers(String keyword);

    void assign(AssignRequest request, Long adminId);

    void revoke(RevokeRequest request, Long adminId);
}
