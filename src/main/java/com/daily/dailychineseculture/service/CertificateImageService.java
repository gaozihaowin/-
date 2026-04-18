package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.entity.Certificate;

public interface CertificateImageService {

    String generateCertificateImage(Certificate certificate);

    int batchGenerateImages(Integer campId);
}