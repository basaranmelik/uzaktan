package com.guzem.uzaktan.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CourseVideoResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String description;
    private Integer orderIndex;
    private String filePath;
    private String originalFileName;
    private LocalDateTime createdAt;
    private boolean watched;
    private boolean locked;
}
