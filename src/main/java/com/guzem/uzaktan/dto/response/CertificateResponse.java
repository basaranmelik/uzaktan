package com.guzem.uzaktan.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CertificateResponse {

    private Long id;
    private Long courseId;
    private String certificateCode;
    private String fileUrl;
    private LocalDateTime issueDate;
    private String courseTitle;
    private String userName;
}
