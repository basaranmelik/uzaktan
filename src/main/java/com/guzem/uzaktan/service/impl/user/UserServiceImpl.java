package com.guzem.uzaktan.service.impl.user;

import com.guzem.uzaktan.config.security.SecurityProperties;
import com.guzem.uzaktan.dto.request.ProfileUpdateRequest;
import com.guzem.uzaktan.dto.request.RegisterRequest;
import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.mapper.user.UserMapper;
import com.guzem.uzaktan.model.common.Address;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final SecurityProperties securityProperties;

    @Override
    public UserResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Şifreler eşleşmiyor.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .birthDate(request.getBirthDate())
                .role(Role.USER)
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userMapper.toResponse(loadUser(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "email", email));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = loadUser(userId);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }

        if (com.guzem.uzaktan.util.PhoneUtils.isProvided(request.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
        } else if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(null);
        }

        Address address = user.getAddress() != null ? user.getAddress() : new Address();
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getDistrict() != null) address.setDistrict(request.getDistrict());
        if (request.getZipCode() != null) address.setZipCode(request.getZipCode());
        if (request.getFullAddress() != null) address.setFullAddress(request.getFullAddress());
        user.setAddress(address);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = loadUser(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mevcut şifre yanlış.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public void toggleUserLock(Long userId) {
        User user = loadUser(userId);
        user.setLocked(!user.isLocked());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty() || phoneNumber.equals("+90 ")) {
            return false;
        }
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = loadUser(userId);
        userRepository.delete(user);
    }

    @Override
    public void changeRole(Long userId, Role role) {
        User user = loadUser(userId);
        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long findUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "email", email));
        return user.getId();
    }

    @Override
    public void recordLoginFailure(String email) {
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
    public void recordLoginSuccess(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getFailedLoginAttempts() > 0 || user.getLockUntil() != null) {
                user.setFailedLoginAttempts(0);
                user.setLockUntil(null);
                userRepository.save(user);
            }
        });
    }

    private User loadUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", id));
    }
}
