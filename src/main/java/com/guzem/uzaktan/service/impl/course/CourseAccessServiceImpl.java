package com.guzem.uzaktan.service.impl.course;

import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.model.course.EnrollmentStatus;
import com.guzem.uzaktan.repository.course.CourseRepository;
import com.guzem.uzaktan.repository.course.EnrollmentRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.course.CourseAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseAccessServiceImpl implements CourseAccessService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public boolean hasActiveAccess(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", userId));

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.FIRM) return true;

        if (user.getRole() == Role.TEACHER) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));
            if (course.getInstructor() != null && course.getInstructor().getId().equals(userId)) {
                return true;
            }
        }

        return enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .map(e -> e.getStatus() == EnrollmentStatus.ACTIVE || e.getStatus() == EnrollmentStatus.COMPLETED)
                .orElse(false);
    }

    @Override
    public boolean isTeacherOrAdminForCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", userId));

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.FIRM) return true;

        if (user.getRole() == Role.TEACHER) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Kurs", "id", courseId));
            return course.getInstructor() != null && course.getInstructor().getId().equals(userId);
        }

        return false;
    }
}
