package com.guzem.uzaktan.service.impl.user;

import com.guzem.uzaktan.config.security.SecurityProperties;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.user.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final UserRepository userRepository;
    private final SecurityProperties securityProperties;

    @Override
    @Transactional
    public void recordFailure(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= securityProperties.getMaxLoginAttempts()) {
                user.setLockUntil(LocalDateTime.now().plusMinutes(securityProperties.getLockoutMinutes()));
                user.setFailedLoginAttempts(0);
            }
            userRepository.save(user);
        });
    }

    @Override
    @Transactional
    public void recordSuccess(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getFailedLoginAttempts() > 0 || user.getLockUntil() != null) {
                user.setFailedLoginAttempts(0);
                user.setLockUntil(null);
                userRepository.save(user);
            }
        });
    }
}
