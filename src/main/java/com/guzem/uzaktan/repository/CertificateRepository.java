package com.guzem.uzaktan.repository;

import com.guzem.uzaktan.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findByUserId(Long userId);

    Optional<Certificate> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    Optional<Certificate> findByCertificateCode(String certificateCode);
}
