package com.guzem.uzaktan.service.user;

import com.guzem.uzaktan.dto.request.TeacherCreateRequest;

public interface TeacherManagementService {

    void createTeacher(TeacherCreateRequest request);

    String createTeacherWithPassword(TeacherCreateRequest request);
}
