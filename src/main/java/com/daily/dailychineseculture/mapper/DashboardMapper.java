package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.vo.CourseAdminDashboardVO;
import com.daily.dailychineseculture.vo.SuperAdminDashboardVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DashboardMapper {
    Integer countTotalUsers();

    Integer countActiveCamps();

    Integer countActiveAdmins();

    Integer countPendingApprovals();

    List<SuperAdminDashboardVO.ApprovalItemVO> selectLatestPendingApprovals(@Param("limit") Integer limit);

    Integer countTotalCamps();

    Integer countActiveCampsForCourse();

    Integer countTotalCampPlans();

    Integer countTotalPlanTasks();

    List<CourseAdminDashboardVO.RecentCampVO> selectRecentCamps(@Param("limit") Integer limit);
}
