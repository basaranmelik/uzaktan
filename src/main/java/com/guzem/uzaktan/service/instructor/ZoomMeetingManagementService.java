package com.guzem.uzaktan.service.instructor;

import com.guzem.uzaktan.dto.request.ZoomMeetingCreateRequest;
import com.guzem.uzaktan.dto.request.ZoomMeetingUpdateRequest;
import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;

import java.util.List;

public interface ZoomMeetingManagementService {

    ZoomMeetingResponse createMeeting(Long courseId, ZoomMeetingCreateRequest request, Long teacherUserId);

    ZoomMeetingResponse updateMeeting(Long meetingId, ZoomMeetingUpdateRequest request, Long teacherUserId);

    void cancelMeeting(Long meetingId, Long teacherUserId);

    void startMeeting(Long meetingId, Long teacherUserId);

    void addRecordingUrl(Long meetingId, String recordingUrl, Long teacherUserId);

    List<ZoomMeetingResponse> findByCourse(Long courseId, Long teacherUserId);

    ZoomMeetingResponse findByIdForTeacher(Long meetingId, Long teacherUserId);

    void generateScheduledMeetings(Long courseId);
}
