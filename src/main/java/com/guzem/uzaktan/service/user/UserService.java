package com.guzem.uzaktan.service.user;

import com.guzem.uzaktan.dto.request.ProfileUpdateRequest;
import com.guzem.uzaktan.dto.request.RegisterRequest;
import com.guzem.uzaktan.dto.response.UserResponse;

public interface UserService extends UserQueryService, UserAdminService {

    UserResponse register(RegisterRequest request);

    UserResponse updateProfile(Long userId, ProfileUpdateRequest request);

    void changePassword(Long userId, String oldPassword, String newPassword);

    void recordLoginFailure(String email);

    void recordLoginSuccess(String email);
}
