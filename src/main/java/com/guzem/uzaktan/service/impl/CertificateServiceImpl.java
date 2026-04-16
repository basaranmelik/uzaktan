package com.guzem.uzaktan.service.impl;

import com.guzem.uzaktan.dto.response.CertificateResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.mapper.CertificateMapper;
import com.guzem.uzaktan.model.Certificate;
import com.guzem.uzaktan.model.Course;
import com.guzem.uzaktan.model.User;
import com.guzem.uzaktan.repository.CertificateRepository;
import com.guzem.uzaktan.repository.CourseRepository;
import com.guzem.uzaktan.repository.UserRepository;
import com.guzem.uzaktan.service.CertificateService;
import com.guzem.uzaktan.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CertificateMapper certificateMapper;
    private final NotificationService notificationService;

    @Override
    public CertificateResponse issueCertificate(Long userId, Long courseId) {
        // İdempotent: zaten varsa mevcut sertifikayı döndür
        return certificateRepository.findByUserIdAndCourseId(userId, courseId)
                .map(certificateMapper::toResponse)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", userId));
                    Course course = courseRepository.findById(courseId)
                            .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));

                    String code = "GAZI-" + courseId + "-" + userId + "-"
                            + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                    Certificate certificate = Certificate.builder()
                            .certificateCode(code)
                            .user(user)
                            .course(course)
                            .build();

                    CertificateResponse saved = certificateMapper.toResponse(certificateRepository.save(certificate));
                    notificationService.create(user, com.guzem.uzaktan.model.NotificationType.CERTIFICATE_ISSUED,
                            "Sertifikanız Hazır",
                            "\"" + course.getTitle() + "\" kursuna ait sertifikanız oluşturuldu.",
                            "/sertifikalarim");
                    return saved;
                });
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userCertificates", key = "#userId")
    public List<CertificateResponse> findByUser(Long userId) {
        return certificateRepository.findByUserId(userId).stream()
                .map(certificateMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponse> findAll() {
        return certificateRepository.findAll().stream()
                .map(certificateMapper::toResponse)
                .toList();
    }

    @Override
    public void revoke(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Sertifika", "id", certificateId));
        certificateRepository.delete(certificate);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "certificate", key = "#code")
    public CertificateResponse findByCode(String code) {
        Certificate certificate = certificateRepository.findByCertificateCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Sertifika", "kod", code));
        return certificateMapper.toResponse(certificate);
    }
}
