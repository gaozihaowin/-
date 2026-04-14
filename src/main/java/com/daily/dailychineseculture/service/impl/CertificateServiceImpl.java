package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.entity.Certificate;
import com.daily.dailychineseculture.mapper.CertificateMapper;
import com.daily.dailychineseculture.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CertificateServiceImpl implements CertificateService {

    @Autowired
    private CertificateMapper certificateMapper;

    @Override
    public List<Certificate> getMyCertificates(Long userId) {
        return certificateMapper.selectByUserId(userId);
    }

    @Override
    public void issueCompletionCertificate(Long userId, String levelName) {
        Certificate cert = new Certificate();
        cert.setUserId(userId);
        cert.setType(levelName);
        cert.setNumber(generateCertificateNumber());
        certificateMapper.insert(cert);
    }

    private String generateCertificateNumber() {
        return "CERT-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
