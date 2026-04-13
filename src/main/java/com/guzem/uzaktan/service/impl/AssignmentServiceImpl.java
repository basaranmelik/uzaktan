package com.guzem.uzaktan.service.impl;

import com.guzem.uzaktan.dto.request.AssignmentCreateRequest;
import com.guzem.uzaktan.dto.request.AssignmentUpdateRequest;
import com.guzem.uzaktan.dto.request.GradeSubmissionRequest;
import com.guzem.uzaktan.dto.request.SubmissionCreateRequest;
import com.guzem.uzaktan.dto.response.AssignmentResponse;
import com.guzem.uzaktan.dto.response.SubmissionResponse;
import com.guzem.uzaktan.exception.DuplicateSubmissionException;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.mapper.AssignmentMapper;
import com.guzem.uzaktan.model.*;
import com.guzem.uzaktan.repository.*;
import com.guzem.uzaktan.service.AssignmentService;
import com.guzem.uzaktan.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final AssignmentMapper assignmentMapper;
    private final FileStorageService fileStorageService;

    // ----------------------------------------------------------------
    // Öğretmen / Admin işlemleri
    // ----------------------------------------------------------------

    @Override
    public AssignmentResponse createAssignment(Long courseId, AssignmentCreateRequest request, Long requestingUserId) {
        Course course = loadCourse(courseId);
        checkTeacherOrAdmin(course, requestingUserId);
        Assignment assignment = assignmentMapper.toEntity(request, course);
        Assignment saved = assignmentRepository.save(assignment);
        return assignmentMapper.toResponse(saved, 0, 0);
    }

    @Override
    public AssignmentResponse updateAssignment(Long assignmentId, AssignmentUpdateRequest request, Long requestingUserId) {
        Assignment assignment = loadAssignment(assignmentId);
        checkTeacherOrAdmin(assignment.getCourse(), requestingUserId);
        if (request.getTitle() != null) assignment.setTitle(request.getTitle());
        if (request.getDescription() != null) assignment.setDescription(request.getDescription());
        if (request.getDueDate() != null) assignment.setDueDate(request.getDueDate());
        if (request.getMaxScore() != null) assignment.setMaxScore(request.getMaxScore());
        Assignment saved = assignmentRepository.save(assignment);
        long subCount = submissionRepository.countByAssignmentId(assignmentId);
        long pending = submissionRepository.countByCourseIdAndStatus(
                saved.getCourse().getId(), SubmissionStatus.SUBMITTED);
        return assignmentMapper.toResponse(saved, subCount, pending);
    }

    @Override
    public void deleteAssignment(Long assignmentId, Long requestingUserId) {
        Assignment assignment = loadAssignment(assignmentId);
        checkTeacherOrAdmin(assignment.getCourse(), requestingUserId);
        assignmentRepository.delete(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> findByCourse(Long courseId) {
        List<Assignment> assignments = assignmentRepository.findByCourseIdOrderByDueDateAsc(courseId);
        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).toList();
        Map<Long, Long> submissionCounts = buildSubmissionCountMap(assignmentIds);
        long pendingCount = submissionRepository.countByCourseIdAndStatus(courseId, SubmissionStatus.SUBMITTED);
        return assignments.stream()
                .map(a -> assignmentMapper.toResponse(a, submissionCounts.getOrDefault(a.getId(), 0L), pendingCount))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse findById(Long assignmentId) {
        Assignment a = loadAssignment(assignmentId);
        long subCount = submissionRepository.countByAssignmentId(assignmentId);
        long pending = submissionRepository.countByCourseIdAndStatus(
                a.getCourse().getId(), SubmissionStatus.SUBMITTED);
        return assignmentMapper.toResponse(a, subCount, pending);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> findSubmissionsByAssignment(Long assignmentId, Long requestingUserId) {
        Assignment assignment = loadAssignment(assignmentId);
        checkTeacherOrAdmin(assignment.getCourse(), requestingUserId);
        return submissionRepository.findByAssignmentIdWithUser(assignmentId)
                .stream()
                .map(assignmentMapper::toSubmissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SubmissionResponse gradeSubmission(Long submissionId, GradeSubmissionRequest request, Long requestingUserId) {
        AssignmentSubmission submission = loadSubmission(submissionId);
        checkTeacherOrAdmin(submission.getAssignment().getCourse(), requestingUserId);
        if (request.getScore() > submission.getAssignment().getMaxScore()) {
            throw new UnauthorizedActionException(
                    "Puan maksimum puanı (" + submission.getAssignment().getMaxScore() + ") aşamaz.");
        }
        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setGradedAt(LocalDateTime.now());
        return assignmentMapper.toSubmissionResponse(submissionRepository.save(submission));
    }

    // ----------------------------------------------------------------
    // Öğrenci işlemleri
    // ----------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> findAssignmentsForStudent(Long userId) {
        return assignmentRepository.findByEnrolledUserId(userId)
                .stream()
                .map(a -> assignmentMapper.toResponse(a, 0, 0))
                .collect(Collectors.toList());
    }

    @Override
    public SubmissionResponse submit(Long assignmentId, Long userId,
                                     SubmissionCreateRequest request,
                                     MultipartFile file) {
        Assignment assignment = loadAssignment(assignmentId);
        validateSubmission(assignment, assignmentId, userId, request, file);

        boolean hasText = request.getTextAnswer() != null && !request.getTextAnswer().isBlank();
        boolean hasFile = file != null && !file.isEmpty();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", userId));

        String filePath = null;
        String originalFileName = null;
        if (hasFile) {
            String baseName = user.getLastName() + "_" + user.getFirstName()
                    + "_" + assignment.getCourse().getTitle()
                    + "_" + assignment.getTitle();
            filePath = storeSubmissionFile(file, assignmentId, baseName);
            originalFileName = file.getOriginalFilename();
        }

        AssignmentSubmission submission = AssignmentSubmission.builder()
                .assignment(assignment)
                .user(user)
                .textAnswer(hasText ? request.getTextAnswer() : null)
                .filePath(filePath)
                .originalFileName(originalFileName)
                .status(SubmissionStatus.SUBMITTED)
                .build();

        return assignmentMapper.toSubmissionResponse(submissionRepository.save(submission));
    }

    @Override
    public SubmissionResponse updateSubmission(Long assignmentId, Long userId,
                                               SubmissionCreateRequest request,
                                               MultipartFile file) {
        Assignment assignment = loadAssignment(assignmentId);

        if (LocalDateTime.now().isAfter(assignment.getDueDate())) {
            throw new UnauthorizedActionException("Son teslim tarihi geçtiği için güncelleme yapılamaz.");
        }

        AssignmentSubmission submission = submissionRepository
                .findByAssignmentIdAndUserId(assignmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Teslim", "assignmentId", assignmentId));

        boolean hasText = request.getTextAnswer() != null && !request.getTextAnswer().isBlank();
        boolean hasFile = file != null && !file.isEmpty();
        if (!hasText && !hasFile && submission.getFilePath() == null && submission.getTextAnswer() == null) {
            throw new UnauthorizedActionException("Metin cevabı veya dosya yüklemelisiniz.");
        }

        if (hasFile) {
            if (submission.getFilePath() != null) {
                fileStorageService.delete(submission.getFilePath());
            }
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", userId));
            String baseName = user.getLastName() + "_" + user.getFirstName()
                    + "_" + assignment.getCourse().getTitle()
                    + "_" + assignment.getTitle();
            submission.setFilePath(storeSubmissionFile(file, assignmentId, baseName));
            submission.setOriginalFileName(file.getOriginalFilename());
        }

        if (request.getTextAnswer() != null) {
            submission.setTextAnswer(request.getTextAnswer().isBlank() ? null : request.getTextAnswer());
        }

        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setScore(null);
        submission.setFeedback(null);
        submission.setGradedAt(null);

        return assignmentMapper.toSubmissionResponse(submissionRepository.save(submission));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubmissionResponse> findSubmission(Long assignmentId, Long userId) {
        return submissionRepository.findByAssignmentIdAndUserId(assignmentId, userId)
                .map(assignmentMapper::toSubmissionResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse findSubmissionById(Long submissionId) {
        return assignmentMapper.toSubmissionResponse(loadSubmission(submissionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> findAllSubmissionsForStudent(Long userId) {
        return submissionRepository.findByUserIdWithAssignment(userId)
                .stream()
                .map(assignmentMapper::toSubmissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> findPendingAssignmentsForStudent(Long userId) {
        List<Assignment> all = assignmentRepository.findByEnrolledUserId(userId);
        Set<Long> submitted = submissionRepository.findByUserIdWithAssignment(userId)
                .stream()
                .map(s -> s.getAssignment().getId())
                .collect(Collectors.toSet());
        return all.stream()
                .filter(a -> !submitted.contains(a.getId()))
                .filter(a -> a.getDueDate().isAfter(LocalDateTime.now()))
                .map(a -> assignmentMapper.toResponse(a, 0, 0))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadSubmissionsZip(Long assignmentId, Long requestingUserId) throws IOException {
        Assignment assignment = loadAssignment(assignmentId);
        checkTeacherOrAdmin(assignment.getCourse(), requestingUserId);

        List<AssignmentSubmission> submissions = submissionRepository.findByAssignmentIdWithUser(assignmentId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            for (AssignmentSubmission s : submissions) {
                if (s.getFilePath() == null) continue;
                Path file = fileStorageService.resolve(s.getFilePath());
                if (!Files.exists(file)) continue;
                String entryName = file.getFileName().toString();
                zip.putNextEntry(new ZipEntry(entryName));
                Files.copy(file, zip);
                zip.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllAssignments() {
        return assignmentRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingSubmissions() {
        return submissionRepository.countByStatus(SubmissionStatus.SUBMITTED);
    }

    // ----------------------------------------------------------------
    // Yardımcı metodlar
    // ----------------------------------------------------------------

    private void validateSubmission(Assignment assignment, Long assignmentId, Long userId,
                                     SubmissionCreateRequest request, MultipartFile file) {
        Long courseId = assignment.getCourse().getId();

        enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE
                        || e.getStatus() == EnrollmentStatus.COMPLETED)
                .orElseThrow(() -> new UnauthorizedActionException(
                        "Bu kursa kayıtlı değilsiniz veya kaydınız aktif değil."));

        if (LocalDateTime.now().isAfter(assignment.getDueDate())) {
            throw new UnauthorizedActionException("Ödev teslim tarihi geçmiştir.");
        }

        if (submissionRepository.existsByAssignmentIdAndUserId(assignmentId, userId)) {
            throw new DuplicateSubmissionException(assignmentId, userId);
        }

        boolean hasText = request.getTextAnswer() != null && !request.getTextAnswer().isBlank();
        boolean hasFile = file != null && !file.isEmpty();
        if (!hasText && !hasFile) {
            throw new UnauthorizedActionException("Metin cevabı veya dosya yüklemelisiniz.");
        }
    }

    private String storeSubmissionFile(MultipartFile file, Long assignmentId, String baseName) {
        try {
            return fileStorageService.storeWithName(file, assignmentId, baseName);
        } catch (IOException e) {
            throw new RuntimeException("Dosya kaydedilemedi: " + e.getMessage(), e);
        }
    }

    private void checkTeacherOrAdmin(Course course, Long requestingUserId) {
        User user = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", requestingUserId));
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isInstructor = course.getInstructor() != null
                && course.getInstructor().getId().equals(requestingUserId);
        if (!isAdmin && !isInstructor) {
            throw new UnauthorizedActionException("Bu kurs üzerinde işlem yapma yetkiniz yok.");
        }
    }

    private Map<Long, Long> buildSubmissionCountMap(List<Long> assignmentIds) {
        if (assignmentIds.isEmpty()) return Map.of();
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : submissionRepository.countByAssignmentIds(assignmentIds)) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }

    private Assignment loadAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ödev", "id", id));
    }

    private AssignmentSubmission loadSubmission(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teslim", "id", id));
    }

    private Course loadCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", id));
    }
}
