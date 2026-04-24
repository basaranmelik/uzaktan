package com.guzem.uzaktan.repository.instructor;

import com.guzem.uzaktan.model.instructor.ZoomMeeting;
import com.guzem.uzaktan.model.instructor.ZoomMeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ZoomMeetingRepository extends JpaRepository<ZoomMeeting, Long> {

    List<ZoomMeeting> findByCourseIdOrderByScheduledAtDesc(Long courseId);

    @Query("""
            SELECT m FROM ZoomMeeting m JOIN FETCH m.course c
            JOIN Enrollment e ON e.course.id = c.id
            WHERE e.user.id = :userId
              AND e.status = ACTIVE
              AND m.status = SCHEDULED
              AND m.scheduledAt >= :now
            ORDER BY m.scheduledAt ASC
            """)
    List<ZoomMeeting> findUpcomingForStudent(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("""
            SELECT m FROM ZoomMeeting m JOIN FETCH m.course c
            JOIN Enrollment e ON e.course.id = c.id
            WHERE e.user.id = :userId
              AND e.status = ACTIVE
              AND m.status = SCHEDULED
            ORDER BY m.scheduledAt DESC
            """)
    List<ZoomMeeting> findAllForStudent(@Param("userId") Long userId);

    @Query("""
            SELECT m FROM ZoomMeeting m JOIN FETCH m.course
            WHERE m.scheduledAt >= :from AND m.scheduledAt < :to
              AND m.status = SCHEDULED
            """)
    List<ZoomMeeting> findScheduledBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
