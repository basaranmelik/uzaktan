package com.guzem.uzaktan.repository.course;

import com.guzem.uzaktan.model.course.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    long countByCourseId(Long courseId);

    List<Question> findByIdInAndCourseId(Set<Long> ids, Long courseId);

    List<Question> findByCourseIdAndModuleIndex(Long courseId, Integer moduleIndex);

    long countByCourseIdAndModuleIndex(Long courseId, Integer moduleIndex);

    @Modifying
    @Query("DELETE FROM Question q WHERE q.course.id = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);
}
