package com.guzem.uzaktan.service.impl.user;

import com.guzem.uzaktan.dto.request.ProfileUpdateRequest;
import com.guzem.uzaktan.dto.request.RegisterRequest;
import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.mapper.user.UserMapper;
import com.guzem.uzaktan.model.common.Address;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.repository.course.CourseRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

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

        if (request.getZoomEmail() != null) {
            user.setZoomEmail(request.getZoomEmail().isBlank() ? null : request.getZoomEmail().trim());
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = loadUser(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mevcut şifre yanlış.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        if (user.isPasswordResetRequired()) {
            user.setPasswordResetRequired(false);
        }
        userRepository.save(user);
    }

    @Override
    @CacheEvict(value = "instructorList", allEntries = true)
    public void updateTeacherFields(Long userId, String bio, String expertise) {
        User user = loadUser(userId);
        if (bio != null) user.setBio(bio);
        if (expertise != null && !expertise.isBlank()) {
            try {
                user.setSkills(objectMapper.writeValueAsString(List.of(expertise)));
            } catch (Exception e) {
                throw new IllegalArgumentException("Uzmanlık alanı işlenemedi.");
            }
        }
        userRepository.save(user);
    }

    @Override
    public void updateProfilePicture(Long userId, String profilePictureUrl) {
        User user = loadUser(userId);
        user.setProfilePictureUrl(profilePictureUrl);
        userRepository.save(user);
    }

    @Override
    public void forceChangePassword(Long userId, String newPassword) {
        User user = loadUser(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetRequired(false);
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
    @CacheEvict(value = "instructorList", allEntries = true)
    public void deleteUser(Long userId) {
        User user = loadUser(userId);

        // Kullanıcının eğitmeni olduğu kurslardaki referansları temizle
        List<Course> instructorCourses = courseRepository.findByInstructorEntityId(userId);
        for (Course course : instructorCourses) {
            course.setInstructor(null);
            if (course.getInstructors() != null) {
                course.getInstructors().remove(user);
            }
        }
        if (!instructorCourses.isEmpty()) {
            courseRepository.saveAll(instructorCourses);
        }

        userRepository.delete(user);
    }

    @Override
    @CacheEvict(value = "instructorList", allEntries = true)
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
    @Cacheable(value = "instructorList", key = "#pageable.pageNumber + '-' + #pageable.pageSize",
               condition = "#role.name() == 'TEACHER'")
    public Page<UserResponse> findUsersByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Long findUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "email", email));
        return user.getId();
    }

    private User loadUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", id));
    }
}
