package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.entity.Certificate;
import java.util.List;

public interface CertificateService {

    List<Certificate> getMyCertificates(Long userId);

    void issueCompletionCertificate(Long userId, String levelName);
}
