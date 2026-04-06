package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.entity.Certificate;
import com.daily.dailychineseculture.mapper.CertificateMapper;
import com.daily.dailychineseculture.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificateServiceImpl implements CertificateService {

    @Autowired
    private CertificateMapper certificateMapper;

    @Override
    public List<Certificate> getMyCertificates(Long userId) {
        return certificateMapper.selectByUserId(userId);
    }
}