package com.guzem.uzaktan.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CourseDocumentResponse {
    private Long id;
    private Long courseId;
    private String title;
    private String filePath;
    private String originalFileName;
    private Long fileSize;
    private String fileType;
    private LocalDateTime createdAt;
}
