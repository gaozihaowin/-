package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.Certificate;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CertificateMapper {
    // 根据用户ID查询证书列表（你的表没有 status 字段，直接查询所有）
    List<Certificate> selectByUserId(Long userId);
}