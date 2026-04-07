package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.vo.CourseAdminDashboardVO;
import com.daily.dailychineseculture.vo.SuperAdminDashboardVO;

public interface DashboardService {
    SuperAdminDashboardVO getSuperAdminDashboard();

    CourseAdminDashboardVO getCourseAdminDashboard();
}
