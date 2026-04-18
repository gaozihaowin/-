package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.entity.CertificateTemplate;

import java.util.List;

public interface CertificateTemplateService {

    List<CertificateTemplate> getAllTemplates();

    List<CertificateTemplate> getTemplatesByCampType(String campType);

    CertificateTemplate getTemplateById(Integer templateId);

    int addTemplate(String name, String imageUrl, String campType);

    boolean deleteTemplate(Integer templateId);
}