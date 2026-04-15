package com.guzem.uzaktan.service;

import com.guzem.uzaktan.dto.request.ZoomMeetingCreateRequest;
import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;

import java.util.List;

public interface ZoomService {

    ZoomMeetingResponse createMeeting(Long courseId, ZoomMeetingCreateRequest request, Long teacherUserId);

    void cancelMeeting(Long meetingId, Long teacherUserId);

    List<ZoomMeetingResponse> findByCourse(Long courseId, Long teacherUserId);

    ZoomMeetingResponse getForStudent(Long meetingId, Long studentUserId);

    ZoomMeetingResponse findByIdForTeacher(Long meetingId, Long teacherUserId);
}
