package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.Certificate;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface CertificateMapper {

    List<Certificate> selectByUserId(Long userId);

    List<Certificate> selectByCampId(@Param("campId") Integer campId);

    @Select("SELECT COUNT(*) FROM t_certificate WHERE user_id = #{userId} AND type = #{type}")
    int countByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    @Insert("INSERT INTO t_certificate (user_id, type, number, issue_time, camp_id, class_id, class_name, student_name, template_id, is_generated) " +
            "VALUES (#{userId}, #{type}, #{number}, NOW(), #{campId}, #{classId}, #{className}, #{studentName}, #{templateId}, #{isGenerated})")
    int insert(Certificate certificate);

    @Update("UPDATE t_certificate SET image_url = #{imageUrl}, award_type = #{awardType}, " +
            "camp_id = #{campId}, class_id = #{classId}, class_name = #{className}, " +
            "student_name = #{studentName}, template_id = #{templateId}, is_generated = #{isGenerated} " +
            "WHERE cert_id = #{certId}")
    int updateImageUrl(Certificate certificate);

    Certificate selectByCertId(@Param("certId") Integer certId);
}
