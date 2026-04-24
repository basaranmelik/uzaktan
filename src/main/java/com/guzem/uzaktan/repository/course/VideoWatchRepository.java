package com.guzem.uzaktan.repository.course;

import com.guzem.uzaktan.model.course.VideoWatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface VideoWatchRepository extends JpaRepository<VideoWatch, Long> {

    Optional<VideoWatch> findByUserIdAndVideoId(Long userId, Long videoId);

    void deleteByVideoId(Long videoId);

    @Query("SELECT vw.video.id FROM VideoWatch vw WHERE vw.user.id = :userId AND vw.video.course.id = :courseId AND vw.completed = true")
    Set<Long> findWatchedVideoIdsByUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Query("SELECT COUNT(vw) FROM VideoWatch vw WHERE vw.user.id = :userId AND vw.video.course.id = :courseId AND vw.completed = true")
    long countWatchedByUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);
}
