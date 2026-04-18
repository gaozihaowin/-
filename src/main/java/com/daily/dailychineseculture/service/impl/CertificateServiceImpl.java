package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.CampEnrollmentDTO;
import com.daily.dailychineseculture.entity.Certificate;
import com.daily.dailychineseculture.entity.CertificateTemplate;
import com.daily.dailychineseculture.mapper.CampEnrollmentMapper;
import com.daily.dailychineseculture.mapper.CertificateMapper;
import com.daily.dailychineseculture.mapper.CertificateTemplateMapper;
import com.daily.dailychineseculture.mapper.ClassMapper;
import com.daily.dailychineseculture.service.CertificateImageService;
import com.daily.dailychineseculture.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CertificateServiceImpl implements CertificateService {

    @Autowired
    private CertificateMapper certificateMapper;

    @Autowired
    private CampEnrollmentMapper enrollmentMapper;

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private CertificateTemplateMapper templateMapper;

    @Autowired
    private CertificateImageService certificateImageService;

    @Override
    public List<Certificate> getMyCertificates(Long userId) {
        return certificateMapper.selectByUserId(userId);
    }

    @Override
    public void issueCompletionCertificate(Long userId, String levelName) {
        issueCompletionCertificate(userId, levelName, null);
    }

    @Override
    public void issueCompletionCertificate(Long userId, String levelName, Integer campId) {
        Certificate cert = new Certificate();
        cert.setUserId(userId);
        cert.setType(levelName);
        cert.setNumber(generateCertificateNumber());
        cert.setIsGenerated(0);

        if (campId != null) {
            cert.setCampId(campId);
            var enrollment = enrollmentMapper.selectByUserIdAndCampId(userId, campId);
            if (enrollment != null) {
                cert.setClassId(enrollment.getClassId());
                cert.setClassName(enrollment.getClassName());
                cert.setStudentName(enrollment.getNickname());
            }
        }

        certificateMapper.insert(cert);
    }

    @Override
    public int issueCertificatesForCamp(Integer campId, Integer templateId) {
        List<CampEnrollmentDTO> enrollments = enrollmentMapper.selectEnrollmentsByCampId(campId);
        int count = 0;

        CertificateTemplate template = null;
        if (templateId != null) {
            template = templateMapper.selectById(templateId);
        }

        for (CampEnrollmentDTO enrollment : enrollments) {
            if (enrollment.getIsCompleted() != null && enrollment.getIsCompleted() == 1) {
                int existing = certificateMapper.countByUserIdAndType(enrollment.getUserId(), enrollment.getClassName());
                if (existing > 0) {
                    continue;
                }

                Certificate cert = new Certificate();
                cert.setUserId(enrollment.getUserId());
                cert.setType(enrollment.getClassName());
                cert.setNumber(generateCertificateNumber());
                cert.setCampId(campId);
                cert.setClassId(enrollment.getClassId());
                cert.setClassName(enrollment.getClassName());
                cert.setStudentName(enrollment.getNickname());
                cert.setTemplateId(templateId);
                cert.setIsGenerated(0);

                certificateMapper.insert(cert);
                count++;
            }
        }

        return count;
    }

    @Override
    public int generateCertificateImages(Integer campId) {
        return certificateImageService.batchGenerateImages(campId);
    }

    @Override
    public String generateSingleCertificateImage(Integer certId) {
        Certificate cert = certificateMapper.selectByCertId(certId);
        if (cert == null) {
            return null;
        }
        return certificateImageService.generateCertificateImage(cert);
    }

    @Override
    public boolean uploadCertificateImage(Integer certId, String imageUrl) {
        Certificate cert = certificateMapper.selectByCertId(certId);
        if (cert == null) {
            return false;
        }
        cert.setImageUrl(imageUrl);
        int rows = certificateMapper.updateImageUrl(cert);
        return rows > 0;
    }

    @Override
    public Certificate getCertificateById(Integer certId) {
        return certificateMapper.selectByCertId(certId);
    }

    @Override
    public List<Certificate> getCertificatesByCampId(Integer campId) {
        return certificateMapper.selectByCampId(campId);
    }

    private String generateCertificateNumber() {
        return "CERT-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
