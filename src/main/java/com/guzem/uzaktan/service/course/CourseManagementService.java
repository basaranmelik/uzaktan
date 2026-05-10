package com.guzem.uzaktan.service.course;

import com.guzem.uzaktan.dto.request.CourseCreateRequest;
import com.guzem.uzaktan.dto.request.CourseUpdateRequest;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.model.course.CourseStatus;
import org.springframework.web.multipart.MultipartFile;

public interface CourseManagementService {

    CourseResponse create(CourseCreateRequest request, MultipartFile image, Long creatorId);

    CourseResponse update(Long id, CourseUpdateRequest request, MultipartFile image);

    void delete(Long id);

    void changeStatus(Long id, CourseStatus newStatus);

    boolean toggleFeatured(Long id);
}
