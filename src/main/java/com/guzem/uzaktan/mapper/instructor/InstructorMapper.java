package com.guzem.uzaktan.mapper.instructor;

import com.guzem.uzaktan.dto.response.InstructorResponse;
import com.guzem.uzaktan.model.instructor.Instructor;
import org.springframework.stereotype.Component;

@Component
public class InstructorMapper {

    public InstructorResponse toResponse(Instructor instructor) {
        return InstructorResponse.builder()
                .id(instructor.getId())
                .name(instructor.getName())
                .bio(instructor.getBio())
                .expertise(instructor.getExpertise())
                .photoUrl(instructor.getPhotoUrl())
                .createdAt(instructor.getCreatedAt())
                .build();
    }
}
