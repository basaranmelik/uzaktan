package com.guzem.uzaktan.service.user;

import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.model.common.Role;

import java.util.List;

/**
 * Read-only user operations — injected by components that only query user data.
 */
public interface UserQueryService {

    UserResponse findById(Long id);

    UserResponse findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    List<UserResponse> findUsersByRole(Role role);

    Long findUserIdByEmail(String email);
}
