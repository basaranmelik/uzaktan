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
import com.guzem.uzaktan.mapper.UserMapper;
import com.guzem.uzaktan.model.*;
import com.guzem.uzaktan.repository.*;
import com.guzem.uzaktan.service.AssignmentService;
import com.guzem.uzaktan.service.EmailService;
import com.guzem.uzaktan.service.FileStorageService;
import com.guzem.uzaktan.service.NotificationService;
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
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    // ----------------------------------------------------------------
    // Öğretmen / Admin işlemleri
    // ----------------------------------------------------------------

    @Override
    public AssignmentResponse createAssignment(Long courseId, AssignmentCreateRequest request, Long requestingUserId) {
        Course course = loadCourse(courseId);
        checkTeacherOrAdmin(course, requestingUserId);
        Assignment assignment = assignmentMapper.toEntity(request, course);
        Assignment saved = assignmentRepository.save(assignment);

        // Aktif öğrencilere bildirim ve mail gönder
        List<Enrollment> activeEnrollments = enrollmentRepository.findActiveEnrollmentsForCourse(courseId);
        for (Enrollment enrollment : activeEnrollments) {
            User student = enrollment.getUser();
            notificationService.create(student, NotificationType.NEW_ASSIGNMENT,
                    "Yeni Ödev Yayınlandı",
                    "\"" + course.getTitle() + "\" kursunda yeni bir ödev yayınlandı: " + saved.getTitle(),
                    "/panom");
            emailService.sendNewAssignmentNotification(student, saved);
        }

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
    public AssignmentResponse findById(Long assignmentId, Long requestingUserId) {
        Assignment a = loadAssignment(assignmentId);
        checkAccess(a, requestingUserId);
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
        if (request.getScore() < 0) {
            throw new IllegalArgumentException("Puan negatif olamaz.");
        }
        if (request.getScore() > submission.getAssignment().getMaxScore()) {
            throw new IllegalArgumentException(
                    "Puan maksimum puanı (" + submission.getAssignment().getMaxScore() + ") aşamaz.");
        }
        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setGradedAt(LocalDateTime.now());
        SubmissionResponse response = assignmentMapper.toSubmissionResponse(submissionRepository.save(submission));
        notificationService.create(submission.getUser(), com.guzem.uzaktan.model.NotificationType.ASSIGNMENT_GRADED,
                "Ödeviniz Notlandırıldı",
                "\"" + submission.getAssignment().getTitle() + "\" ödeviniz notlandırıldı: " + request.getScore() + " puan.",
                "/panom");
        emailService.sendAssignmentGradedToStudent(submission);
        return response;
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
            String baseName = assignment.getCourse().getTitle()
                    + "_" + assignment.getTitle()
                    + "_" + user.getFirstName() + "_" + user.getLastName();
            filePath = storeSubmissionFile(file, assignmentId, assignment.getCourse().getTitle(), baseName);
            originalFileName = java.nio.file.Paths.get(filePath).getFileName().toString();
        }

        AssignmentSubmission submission = AssignmentSubmission.builder()
                .assignment(assignment)
                .user(user)
                .textAnswer(hasText ? request.getTextAnswer() : null)
                .filePath(filePath)
                .originalFileName(originalFileName)
                .status(SubmissionStatus.SUBMITTED)
                .build();

        AssignmentSubmission saved = submissionRepository.save(submission);
        // Öğretmene e-posta bildirimi
        User teacher = assignment.getCourse().getInstructor();
        if (teacher != null) {
            emailService.sendAssignmentSubmittedToTeacher(teacher, saved);
        }
        return assignmentMapper.toSubmissionResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubmissionResponse> findSubmission(Long assignmentId, Long userId) {
        return submissionRepository.findByAssignmentIdAndUserId(assignmentId, userId)
                .map(assignmentMapper::toSubmissionResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse findSubmissionById(Long submissionId, Long requestingUserId) {
        AssignmentSubmission submission = loadSubmission(submissionId);
        checkSubmissionAccess(submission, requestingUserId);
        return assignmentMapper.toSubmissionResponse(submission);
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
    public List<com.guzem.uzaktan.dto.response.UserResponse> findNotSubmittedStudents(Long courseId, Long assignmentId) {
        return enrollmentRepository.findActiveUsersWithoutSubmission(courseId, assignmentId)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> findOverdueAssignmentsForStudent(Long userId) {
        List<Assignment> all = assignmentRepository.findByEnrolledUserId(userId);
        Set<Long> submitted = submissionRepository.findByUserIdWithAssignment(userId)
                .stream()
                .map(s -> s.getAssignment().getId())
                .collect(Collectors.toSet());
        return all.stream()
                .filter(a -> !submitted.contains(a.getId()))
                .filter(a -> a.getDueDate().isBefore(LocalDateTime.now()))
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

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> findAllAssignmentsForAdmin() {
        List<Assignment> assignments = assignmentRepository.findAll();
        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).toList();
        Map<Long, Long> submissionCounts = buildSubmissionCountMap(assignmentIds);
        
        return assignments.stream().map(a -> {
            long pendingCount = submissionRepository.countByCourseIdAndStatus(a.getCourse().getId(), SubmissionStatus.SUBMITTED);
            return assignmentMapper.toResponse(a, submissionCounts.getOrDefault(a.getId(), 0L), pendingCount);
        }).collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // Yardımcı metodlar
    // ----------------------------------------------------------------

    private void checkAccess(Assignment assignment, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", userId));

        // Admin her şeyi görür
        if (user.getRole() == Role.ADMIN) return;

        // Eğitmen kendi kursunun ödevini görür
        if (user.getRole() == Role.TEACHER &&
                assignment.getCourse().getInstructor() != null &&
                assignment.getCourse().getInstructor().getId().equals(userId)) return;

        // Öğrenci kayıtlı olduğu kursun ödevini görür
        if (user.getRole() == Role.USER &&
                enrollmentRepository.existsByUserIdAndCourseIdAndStatus(userId, assignment.getCourse().getId(), EnrollmentStatus.ACTIVE)) return;

        throw new UnauthorizedActionException("Bu ödeve erişim yetkiniz bulunmamaktadır.");
    }

    private void checkSubmissionAccess(AssignmentSubmission submission, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", userId));

        // Admin her şeyi görür
        if (user.getRole() == Role.ADMIN) return;

        // Eğitmen kendi kursunun ödev teslimlerini görür
        if (user.getRole() == Role.TEACHER &&
                submission.getAssignment().getCourse().getInstructor() != null &&
                submission.getAssignment().getCourse().getInstructor().getId().equals(userId)) return;

        // Öğrenci kendi teslimini görür
        if (user.getRole() == Role.USER && submission.getUser().getId().equals(userId)) return;

        throw new UnauthorizedActionException("Bu ödev teslimine erişim yetkiniz bulunmamaktadır.");
    }

    private void validateSubmission(Assignment assignment, Long assignmentId, Long userId,
                                     SubmissionCreateRequest request, MultipartFile file) {
        Long courseId = assignment.getCourse().getId();

        enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE
                        || e.getStatus() == EnrollmentStatus.COMPLETED)
                .orElseThrow(() -> new UnauthorizedActionException("Bu ödeve erişmek için aktif kayıt olmanız gerekmektedir."));

        if (LocalDateTime.now().isAfter(assignment.getDueDate())) {
            throw new IllegalArgumentException("Ödev teslim tarihi geçmiştir.");
        }

        if (submissionRepository.existsByAssignmentIdAndUserId(assignmentId, userId)) {
            throw new DuplicateSubmissionException(assignmentId, userId);
        }

        boolean hasFile = file != null && !file.isEmpty();
        if (!hasFile) {
            throw new IllegalArgumentException("Dosya yüklemeniz zorunludur.");
        }
    }

    private String storeSubmissionFile(MultipartFile file, Long assignmentId, String courseTitle, String baseName) {
        try {
            return fileStorageService.storeWithName(file, assignmentId, courseTitle, baseName);
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
            throw new UnauthorizedActionException("Bu işlem için yetkiniz bulunmamaktadır.");
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
