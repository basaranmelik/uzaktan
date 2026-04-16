package com.guzem.uzaktan.config;

import com.guzem.uzaktan.model.Role;
import com.guzem.uzaktan.model.User;
import com.guzem.uzaktan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (userRepository.findByEmail("admin@guzem.gazi.edu.tr").isEmpty()) {
            User admin = User.builder()
                    .email("admin@guzem.gazi.edu.tr")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("GUZEM")
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Default admin kullanıcısı oluşturuldu — kullanıcı adı: admin");
        }
    }
}
