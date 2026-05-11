package com.guzem.uzaktan.repository.course;

import com.guzem.uzaktan.model.course.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findByUserId(Long userId);

    List<Certificate> findByCourseId(Long courseId);

    Optional<Certificate> findByUserIdAndCourseId(Long userId, Long courseId);

    Optional<Certificate> findByCertificateCode(String certificateCode);

    @Modifying
    @Query("DELETE FROM Certificate c WHERE c.course.id = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);
}
