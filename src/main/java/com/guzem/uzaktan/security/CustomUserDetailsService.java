package com.guzem.uzaktan.security;

import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Geçersiz kimlik bilgileri."));

        boolean temporarilyLocked = user.getLockUntil() != null
                && user.getLockUntil().isAfter(java.time.LocalDateTime.now());

        return new CustomUserDetails(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                !(user.isLocked() || temporarilyLocked),
                List.of(new SimpleGrantedAuthority(user.getRole().getAuthority())),
                user.getId(),
                user.isPasswordResetRequired()
        );
    }
}
