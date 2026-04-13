package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.entity.Certificate;
import java.util.List;

public interface CertificateService {
    // 获取当前用户的有效证书列表
    List<Certificate> getMyCertificates(Long userId);
}