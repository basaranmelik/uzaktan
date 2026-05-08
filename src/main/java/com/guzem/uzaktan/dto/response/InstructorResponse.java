package com.guzem.uzaktan.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InstructorResponse {

    private Long id;
    private String name;
    private String expertise;
    private String photoUrl;
    private long courseCount;
    private long studentCount;
}
