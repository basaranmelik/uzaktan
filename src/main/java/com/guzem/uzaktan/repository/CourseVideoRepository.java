package com.guzem.uzaktan.repository;

import com.guzem.uzaktan.model.CourseVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseVideoRepository extends JpaRepository<CourseVideo, Long> {

    @Query("SELECT v FROM CourseVideo v WHERE v.course.id = :courseId ORDER BY v.orderIndex ASC")
    List<CourseVideo> findByCourseIdOrderByOrderIndex(@Param("courseId") Long courseId);

    long countByCourseId(Long courseId);
}
