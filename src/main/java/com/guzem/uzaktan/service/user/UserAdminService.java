package com.guzem.uzaktan.service.user;

import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.model.common.Role;

import java.util.List;

/**
 * Admin-level user management operations — injected only by admin-facing components.
 */
public interface UserAdminService {

    List<UserResponse> findAllUsers();

    void toggleUserLock(Long userId);

    void deleteUser(Long userId);

    void changeRole(Long userId, Role role);
}
