package com.guzem.uzaktan.mapper.instructor;

import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;
import com.guzem.uzaktan.model.instructor.ZoomMeeting;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Maps ZoomMeeting entities to ZoomMeetingResponse DTOs.
 * Extracted from ZoomServiceImpl to keep mapping logic out of service classes.
 */
@Component
public class ZoomMeetingMapper {

    public ZoomMeetingResponse toResponse(ZoomMeeting m) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = m.getScheduledAt().plusMinutes(m.getDurationMinutes());
        boolean past = end.isBefore(now);
        boolean live = !past && m.getScheduledAt().isBefore(now);
        return ZoomMeetingResponse.builder()
                .id(m.getId()).topic(m.getTopic()).joinUrl(m.getJoinUrl()).startUrl(m.getStartUrl())
                .password(m.getPassword()).scheduledAt(m.getScheduledAt()).durationMinutes(m.getDurationMinutes())
                .status(m.getStatus()).statusDisplayName(m.getStatus().getDisplayName())
                .courseId(m.getCourse().getId()).courseTitle(m.getCourse().getTitle())
                .recordingUrl(m.getRecordingUrl()).past(past).live(live).hostJoined(m.isHostJoined())
                .startedAt(m.getStartedAt())
                .instructorName(m.getCourse().getInstructor() != null
                        ? m.getCourse().getInstructor().getFirstName() + " " + m.getCourse().getInstructor().getLastName() : null)
                .createdAt(m.getCreatedAt()).build();
    }
}
