package com.guzem.uzaktan.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InstructorResponse {

    private Long id;
    private String name;
    private String bio;
    private String expertise;
    private String photoUrl;
    private LocalDateTime createdAt;
    private long courseCount;
    private long studentCount;
}
