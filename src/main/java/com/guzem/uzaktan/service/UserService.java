package com.guzem.uzaktan.service;

import com.guzem.uzaktan.dto.request.ProfileUpdateRequest;
import com.guzem.uzaktan.dto.request.RegisterRequest;
import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.model.Role;

import java.util.List;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse findById(Long id);

    UserResponse findByEmail(String email);

    UserResponse updateProfile(Long userId, ProfileUpdateRequest request);

    void changePassword(Long userId, String oldPassword, String newPassword);

    List<UserResponse> findAllUsers();

    void toggleUserLock(Long userId);

    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);

    void deleteUser(Long userId);

    void changeRole(Long userId, Role role);

    List<UserResponse> findUsersByRole(Role role);

    Long findUserIdByEmail(String email);
}
