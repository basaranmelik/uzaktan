package com.guzem.uzaktan.service.instructor;

import com.guzem.uzaktan.dto.request.InstructorCreateRequest;
import com.guzem.uzaktan.dto.request.InstructorUpdateRequest;
import com.guzem.uzaktan.dto.response.InstructorResponse;

import java.util.List;
import java.util.Optional;

public interface InstructorService {

    List<InstructorResponse> findAll();

    InstructorResponse findById(Long id);

    Optional<InstructorResponse> findByName(String name);

    InstructorResponse create(InstructorCreateRequest request);

    InstructorResponse update(Long id, InstructorUpdateRequest request);

    void delete(Long id);
}
