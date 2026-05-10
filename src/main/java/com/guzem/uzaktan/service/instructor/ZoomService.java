package com.guzem.uzaktan.service.instructor;

public interface ZoomService extends ZoomMeetingManagementService, ZoomStudentService, ZoomAdminService {

    void processRecordingCompleted(String zoomMeetingId);

    void markMeetingAsStarted(String zoomMeetingId);
}
