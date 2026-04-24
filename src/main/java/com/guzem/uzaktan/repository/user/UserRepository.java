package com.guzem.uzaktan.repository.user;

import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.common.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    List<User> findByRole(Role role);
}
