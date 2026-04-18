package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.CertificateTemplate;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CertificateTemplateMapper {

    @Select("SELECT * FROM t_certificate_template ORDER BY create_time DESC")
    List<CertificateTemplate> selectAll();

    @Select("SELECT * FROM t_certificate_template WHERE camp_type = #{campType}")
    List<CertificateTemplate> selectByCampType(@Param("campType") String campType);

    @Select("SELECT * FROM t_certificate_template WHERE template_id = #{templateId}")
    CertificateTemplate selectById(@Param("templateId") Integer templateId);

    @Insert("INSERT INTO t_certificate_template (name, image_url, camp_type, create_time) " +
            "VALUES (#{name}, #{imageUrl}, #{campType}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "templateId")
    int insert(CertificateTemplate template);

    int update(CertificateTemplate template);

    @Delete("DELETE FROM t_certificate_template WHERE template_id = #{templateId}")
    int deleteById(@Param("templateId") Integer templateId);
}