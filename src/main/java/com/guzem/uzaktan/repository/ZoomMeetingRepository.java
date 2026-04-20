package com.guzem.uzaktan.repository;

import com.guzem.uzaktan.model.ZoomMeeting;
import com.guzem.uzaktan.model.ZoomMeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ZoomMeetingRepository extends JpaRepository<ZoomMeeting, Long> {

    List<ZoomMeeting> findByCourseIdOrderByScheduledAtDesc(Long courseId);

    List<ZoomMeeting> findByCourseIdAndStatusOrderByScheduledAtDesc(Long courseId, ZoomMeetingStatus status);

    @Query("""
            SELECT m FROM ZoomMeeting m JOIN FETCH m.course c
            JOIN Enrollment e ON e.course.id = c.id
            WHERE e.user.id = :userId
              AND e.status = com.guzem.uzaktan.model.EnrollmentStatus.ACTIVE
              AND m.status = com.guzem.uzaktan.model.ZoomMeetingStatus.SCHEDULED
              AND m.scheduledAt >= :now
            ORDER BY m.scheduledAt ASC
            """)
    List<ZoomMeeting> findUpcomingForStudent(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("""
            SELECT m FROM ZoomMeeting m JOIN FETCH m.course c
            JOIN Enrollment e ON e.course.id = c.id
            WHERE e.user.id = :userId
              AND e.status = com.guzem.uzaktan.model.EnrollmentStatus.ACTIVE
              AND m.status = com.guzem.uzaktan.model.ZoomMeetingStatus.SCHEDULED
            ORDER BY m.scheduledAt DESC
            """)
    List<ZoomMeeting> findAllForStudent(@Param("userId") Long userId);

    @Query("""
            SELECT m FROM ZoomMeeting m JOIN FETCH m.course
            WHERE m.scheduledAt >= :from AND m.scheduledAt < :to
              AND m.status = com.guzem.uzaktan.model.ZoomMeetingStatus.SCHEDULED
            """)
    List<ZoomMeeting> findScheduledBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
