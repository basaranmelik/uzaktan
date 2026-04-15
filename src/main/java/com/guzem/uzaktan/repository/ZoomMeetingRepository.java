package com.guzem.uzaktan.repository;

import com.guzem.uzaktan.model.ZoomMeeting;
import com.guzem.uzaktan.model.ZoomMeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZoomMeetingRepository extends JpaRepository<ZoomMeeting, Long> {

    List<ZoomMeeting> findByCourseIdOrderByScheduledAtDesc(Long courseId);

    List<ZoomMeeting> findByCourseIdAndStatusOrderByScheduledAtDesc(Long courseId, ZoomMeetingStatus status);
}
