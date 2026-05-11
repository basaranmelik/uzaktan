package com.guzem.uzaktan.repository.course;

import com.guzem.uzaktan.model.course.CourseVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseVideoRepository extends JpaRepository<CourseVideo, Long> {

    @Query("SELECT v FROM CourseVideo v WHERE v.course.id = :courseId ORDER BY v.orderIndex ASC")
    List<CourseVideo> findByCourseIdOrderByOrderIndex(@Param("courseId") Long courseId);

    @Query("SELECT v.id FROM CourseVideo v WHERE v.course.id = :courseId")
    List<Long> findIdsByCourseId(@Param("courseId") Long courseId);

    long countByCourseId(Long courseId);

    @Modifying
    @Query("DELETE FROM CourseVideo v WHERE v.course.id = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);
}
