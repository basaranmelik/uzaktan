package com.guzem.uzaktan.service;

import com.guzem.uzaktan.dto.request.ZoomMeetingCreateRequest;
import com.guzem.uzaktan.dto.request.ZoomMeetingUpdateRequest;
import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;

import java.util.List;

public interface ZoomService {

    ZoomMeetingResponse createMeeting(Long courseId, ZoomMeetingCreateRequest request, Long teacherUserId);

    ZoomMeetingResponse updateMeeting(Long meetingId, ZoomMeetingUpdateRequest request, Long teacherUserId);

    void cancelMeeting(Long meetingId, Long teacherUserId);

    void addRecordingUrl(Long meetingId, String recordingUrl, Long teacherUserId);

    List<ZoomMeetingResponse> findByCourse(Long courseId, Long teacherUserId);

    ZoomMeetingResponse getForStudent(Long meetingId, Long studentUserId);

    List<ZoomMeetingResponse> getUpcomingForStudent(Long studentUserId);

    ZoomMeetingResponse findByIdForTeacher(Long meetingId, Long teacherUserId);
}
