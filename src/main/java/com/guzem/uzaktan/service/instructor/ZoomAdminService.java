package com.guzem.uzaktan.service.instructor;

import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;

import java.util.List;

public interface ZoomAdminService {

    long countAllMeetings();

    long countMissedMeetings();

    long countRecordedMeetings();

    long countStartedMeetings();

    List<ZoomMeetingResponse> findAllForAdmin();

    List<ZoomMeetingResponse> findAllForAdminByCourse(Long courseId);
}
