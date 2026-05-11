package com.guzem.uzaktan.service.impl.user;

import com.guzem.uzaktan.dto.request.TeacherCreateRequest;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.common.GeneralEmailService;
import com.guzem.uzaktan.service.user.TeacherManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TeacherManagementServiceImpl implements TeacherManagementService {

    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeneralEmailService emailService;
    private final ObjectMapper objectMapper;

    @Override
    @CacheEvict(value = "instructorList", allEntries = true)
    public void createTeacher(TeacherCreateRequest request) {
        createTeacherWithPassword(request);
    }

    @Override
    @CacheEvict(value = "instructorList", allEntries = true)
    public String createTeacherWithPassword(TeacherCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresi zaten kayıtlı.");
        }

        String generatedPassword = generateRandomPassword(16);

        String skillsJson = null;
        if (request.getExpertise() != null && !request.getExpertise().isBlank()) {
            try {
                skillsJson = objectMapper.writeValueAsString(List.of(request.getExpertise()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Uzmanlık alanı işlenemedi.");
            }
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(generatedPassword))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.TEACHER)
                .bio(request.getBio())
                .skills(skillsJson)
                .zoomEmail(request.getZoomEmail() != null && !request.getZoomEmail().isBlank() ? request.getZoomEmail().trim() : null)
                .isPasswordResetRequired(true)
                .build();

        userRepository.save(user);
        emailService.sendTeacherWelcomeEmail(request.getEmail(),
                request.getFirstName() + " " + request.getLastName(), generatedPassword);
        return generatedPassword;
    }

    private String generateRandomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
