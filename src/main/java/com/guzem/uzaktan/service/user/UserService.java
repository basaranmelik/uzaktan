package com.guzem.uzaktan.service.user;

import com.guzem.uzaktan.dto.request.ProfileUpdateRequest;
import com.guzem.uzaktan.dto.request.RegisterRequest;
import com.guzem.uzaktan.dto.request.TeacherCreateRequest;
import com.guzem.uzaktan.dto.response.UserResponse;

public interface UserService extends UserQueryService, UserAdminService {

    UserResponse register(RegisterRequest request);

    /** Creates a TEACHER user with auto-generated password. Returns the generated plaintext password. */
    String createTeacher(TeacherCreateRequest request);

    void updateTeacherFields(Long userId, String bio, String expertise);

    void updateProfilePicture(Long userId, String profilePictureUrl);

    UserResponse updateProfile(Long userId, ProfileUpdateRequest request);

    void changePassword(Long userId, String oldPassword, String newPassword);

    void forceChangePassword(Long userId, String newPassword);

    void recordLoginFailure(String email);

    void recordLoginSuccess(String email);
}
