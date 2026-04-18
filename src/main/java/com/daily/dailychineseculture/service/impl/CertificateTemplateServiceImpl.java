package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.entity.CertificateTemplate;
import com.daily.dailychineseculture.mapper.CertificateTemplateMapper;
import com.daily.dailychineseculture.service.CertificateTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificateTemplateServiceImpl implements CertificateTemplateService {

    @Autowired
    private CertificateTemplateMapper templateMapper;

    @Override
    public List<CertificateTemplate> getAllTemplates() {
        return templateMapper.selectAll();
    }

    @Override
    public List<CertificateTemplate> getTemplatesByCampType(String campType) {
        return templateMapper.selectByCampType(campType);
    }

    @Override
    public CertificateTemplate getTemplateById(Integer templateId) {
        return templateMapper.selectById(templateId);
    }

    @Override
    public int addTemplate(String name, String imageUrl, String campType) {
        CertificateTemplate template = new CertificateTemplate();
        template.setName(name);
        template.setImageUrl(imageUrl);
        template.setCampType(campType);
        return templateMapper.insert(template);
    }

    @Override
    public boolean deleteTemplate(Integer templateId) {
        return templateMapper.deleteById(templateId) > 0;
    }
}