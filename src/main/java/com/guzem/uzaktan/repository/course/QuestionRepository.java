package com.guzem.uzaktan.repository.course;

import com.guzem.uzaktan.model.course.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    long countByCourseId(Long courseId);
}
