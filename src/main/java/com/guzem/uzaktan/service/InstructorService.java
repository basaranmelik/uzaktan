package com.guzem.uzaktan.service;

import com.guzem.uzaktan.dto.request.InstructorCreateRequest;
import com.guzem.uzaktan.dto.request.InstructorUpdateRequest;
import com.guzem.uzaktan.dto.response.InstructorResponse;

import java.util.List;

public interface InstructorService {

    List<InstructorResponse> findAll();

    InstructorResponse findById(Long id);

    InstructorResponse create(InstructorCreateRequest request);

    InstructorResponse update(Long id, InstructorUpdateRequest request);

    void delete(Long id);
}
