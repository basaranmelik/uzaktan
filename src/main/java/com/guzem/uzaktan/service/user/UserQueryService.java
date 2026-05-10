package com.guzem.uzaktan.service.user;

import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.model.common.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserQueryService {

    UserResponse findById(Long id);

    UserResponse findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    List<UserResponse> findUsersByRole(Role role);

    Page<UserResponse> findUsersByRole(Role role, Pageable pageable);

    Long findUserIdByEmail(String email);
}
