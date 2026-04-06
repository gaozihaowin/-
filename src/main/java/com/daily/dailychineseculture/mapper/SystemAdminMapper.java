package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.DutyAssignment;
import com.daily.dailychineseculture.vo.ActiveRoleVO;
import com.daily.dailychineseculture.vo.AdminStatsVO;
import com.daily.dailychineseculture.vo.AdminUserAggVO;
import com.daily.dailychineseculture.vo.SystemAdminVO;
import com.daily.dailychineseculture.vo.UserSearchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SystemAdminMapper {

    AdminStatsVO selectAdminStats();

    List<SystemAdminVO> selectAdminList(@Param("keyword") String keyword,
                                        @Param("offset") Integer offset,
                                        @Param("limit") Integer limit);

    Long countAdminList(@Param("keyword") String keyword);

    List<UserSearchVO> searchUsers(@Param("keyword") String keyword);

    int insertAssignment(@Param("userId") Long userId,
                         @Param("dutyType") String dutyType);

    int existsValidAssignment(@Param("userId") Long userId,
                               @Param("dutyType") String dutyType);

    int deleteAssignment(@Param("assignmentId") Integer assignmentId);

    DutyAssignment selectAssignmentById(@Param("assignmentId") Integer assignmentId);

    List<AdminUserAggVO> selectAdminUserAggRows(@Param("keyword") String keyword);

    List<ActiveRoleVO> selectActiveRolesByUserId(@Param("userId") Long userId);
}
