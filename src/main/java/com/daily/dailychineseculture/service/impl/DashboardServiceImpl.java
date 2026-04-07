package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.mapper.DashboardMapper;
import com.daily.dailychineseculture.service.DashboardService;
import com.daily.dailychineseculture.vo.CourseAdminDashboardVO;
import com.daily.dailychineseculture.vo.SuperAdminDashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;

    @Override
    public SuperAdminDashboardVO getSuperAdminDashboard() {
        SuperAdminDashboardVO vo = new SuperAdminDashboardVO();
        vo.setTotalUsers(dashboardMapper.countTotalUsers());
        vo.setActiveCamps(dashboardMapper.countActiveCamps());
        vo.setActiveAdmins(dashboardMapper.countActiveAdmins());
        vo.setPendingApprovalsCount(dashboardMapper.countPendingApprovals());
        vo.setLatestPendingApprovals(dashboardMapper.selectLatestPendingApprovals(5));
        return vo;
    }

    @Override
    public CourseAdminDashboardVO getCourseAdminDashboard() {
        CourseAdminDashboardVO vo = new CourseAdminDashboardVO();
        vo.setTotalCamps(dashboardMapper.countTotalCamps());
        vo.setActiveCamps(dashboardMapper.countActiveCampsForCourse());
        vo.setTotalCampPlans(dashboardMapper.countTotalCampPlans());
        vo.setTotalPlanTasks(dashboardMapper.countTotalPlanTasks());
        vo.setRecentCamps(dashboardMapper.selectRecentCamps(5));
        return vo;
    }
}
