package com.guzem.uzaktan.config;

import com.guzem.uzaktan.model.course.CourseCategory;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.repository.course.CourseCategoryRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.security.SecureRandom;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseCategoryRepository categoryRepository;
    private final Environment environment;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        seedCategories();
        migrateCourseType();

        if (userRepository.findByEmail("admin@guzem.gazi.edu.tr").isEmpty()) {
            String tempPassword = generateTempPassword();
            User admin = User.builder()
                    .email("admin@guzem.gazi.edu.tr")
                    .password(passwordEncoder.encode(tempPassword))
                    .firstName("Admin")
                    .lastName("GUZEM")
                    .role(Role.ADMIN)
                    .isPasswordResetRequired(true)
                    .build();
            userRepository.save(admin);
            log.warn("=================================================================");
            log.warn("  Default admin created — CHANGE PASSWORD ON FIRST LOGIN");
            log.warn("  Email   : admin@guzem.gazi.edu.tr");
            if (!List.of(environment.getActiveProfiles()).contains("docker")) {
                log.warn("  Password: {}", tempPassword);
            } else {
                log.warn("  Password: [hidden in docker profile — check email or reset]");
            }
            log.warn("=================================================================");
        }
    }

    private void migrateCourseType() {
        try {
            int updated = entityManager.createNativeQuery(
                    "UPDATE course SET course_type = 'HYBRID' WHERE course_type = 'REMOTE_FORMAL'"
            ).executeUpdate();
            if (updated > 0) {
                log.info("Migrated {} course(s) from REMOTE_FORMAL to HYBRID", updated);
            }
        } catch (DataAccessException e) {
            log.warn("Course type migration skipped: {}", e.getMessage());
        }
    }

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
    private static final SecureRandom RNG = new SecureRandom();

    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(CHARS.charAt(RNG.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private void seedCategories() {
        List<String> defaults = List.of(
            "Yazılım Geliştirme",
            "Veri Bilimi",
            "Matematik",
            "Mühendislik",
            "Tasarım",
            "Yabancı Dil",
            "İşletme & Yönetim",
            "Fen Bilimleri",
            "Diğer"
        );
        for (String name : defaults) {
            if (!categoryRepository.existsByDisplayNameIgnoreCase(name)) {
                categoryRepository.save(CourseCategory.builder().displayName(name).build());
            }
        }
    }
}
