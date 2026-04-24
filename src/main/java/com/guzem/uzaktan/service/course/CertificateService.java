package com.guzem.uzaktan.service.course;

import com.guzem.uzaktan.dto.response.CertificateResponse;

import java.util.List;

public interface CertificateService {

    CertificateResponse issueCertificate(Long userId, Long courseId);

    List<CertificateResponse> findByUser(Long userId);

    CertificateResponse findByCode(String code);

    List<CertificateResponse> findAll();

    void revoke(Long certificateId);
}
