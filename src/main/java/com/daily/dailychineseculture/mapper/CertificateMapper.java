package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.Certificate;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CertificateMapper {

    List<Certificate> selectByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM t_certificate WHERE user_id = #{userId} AND type = #{type}")
    int countByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    @Insert("INSERT INTO t_certificate (user_id, type, number, issue_time) " +
            "VALUES (#{userId}, #{type}, #{number}, NOW())")
    int insert(Certificate certificate);
}
