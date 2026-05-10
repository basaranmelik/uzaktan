package com.guzem.uzaktan.service.instructor;

import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;

import java.util.List;

public interface ZoomStudentService {

    ZoomMeetingResponse getForStudent(Long meetingId, Long studentUserId);

    List<ZoomMeetingResponse> getUpcomingForStudent(Long studentUserId);

    List<ZoomMeetingResponse> getAllForStudent(Long studentUserId);
}
