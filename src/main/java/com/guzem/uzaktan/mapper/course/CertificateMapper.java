package com.guzem.uzaktan.mapper.course;

import com.guzem.uzaktan.dto.response.CertificateResponse;
import com.guzem.uzaktan.model.course.Certificate;
import org.springframework.stereotype.Component;

@Component
public class CertificateMapper {

    public CertificateResponse toResponse(Certificate certificate) {
        String userName = certificate.getUser().getFirstName()
                + " " + certificate.getUser().getLastName();

        return CertificateResponse.builder()
                .id(certificate.getId())
                .courseId(certificate.getCourse().getId())
                .certificateCode(certificate.getCertificateCode())
                .fileUrl(certificate.getFileUrl())
                .issueDate(certificate.getIssueDate())
                .courseTitle(certificate.getCourse().getTitle())
                .userName(userName)
                .build();
    }
}
