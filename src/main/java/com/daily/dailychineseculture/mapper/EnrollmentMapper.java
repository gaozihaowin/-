package com.daily.dailychineseculture.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 学员报名 Mapper 接口
 * 使用原生 MyBatis 注解方式实现
 */
@Mapper
public interface EnrollmentMapper {
    
    /**
     * 检查用户是否已报名指定营期
     * 
     * @param userId 用户 ID
     * @param campId 营期 ID
     * @return 符合条件的记录数，大于 0 表示已报名
     */
    @Select("SELECT COUNT(*) " +
            "FROM t_camp_enrollment " +
            "WHERE user_id = #{userId} " +
            "AND camp_id = #{campId}")
    Integer checkEnrollment(@Param("userId") Long userId, @Param("campId") Integer campId);
}
