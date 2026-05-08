package com.guzem.uzaktan.service.course;

import com.guzem.uzaktan.dto.response.CourseDocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourseDocumentService {

    CourseDocumentResponse upload(Long courseId, String title, MultipartFile file, Long instructorId);

    List<CourseDocumentResponse> findByCourse(Long courseId, Long instructorId);

    List<CourseDocumentResponse> findByCourse(Long courseId);

    void delete(Long documentId, Long instructorId);
}
