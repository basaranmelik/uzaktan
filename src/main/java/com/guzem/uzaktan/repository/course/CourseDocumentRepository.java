package com.guzem.uzaktan.repository.course;

import com.guzem.uzaktan.model.course.CourseDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseDocumentRepository extends JpaRepository<CourseDocument, Long> {

    List<CourseDocument> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    void deleteByCourseId(Long courseId);
}
