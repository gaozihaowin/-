package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.entity.Certificate;
import java.util.List;

public interface CertificateService {

    List<Certificate> getMyCertificates(Long userId);

    void issueCompletionCertificate(Long userId, String levelName);

    void issueCompletionCertificate(Long userId, String levelName, Integer campId);

    int issueCertificatesForCamp(Integer campId, Integer templateId);

    int generateCertificateImages(Integer campId);

    String generateSingleCertificateImage(Integer certId);

    boolean uploadCertificateImage(Integer certId, String imageUrl);

    Certificate getCertificateById(Integer certId);

    List<Certificate> getCertificatesByCampId(Integer campId);
}
