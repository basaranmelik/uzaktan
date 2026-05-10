package com.guzem.uzaktan.util;

import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.model.course.Course;
import com.guzem.uzaktan.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessControlUtil {

    private final UserRepository userRepository;

    public void checkTeacherOrAdmin(Course course, Long requestingUserId) {
        User user = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", requestingUserId));
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isInstructor = course.getInstructor() != null
                && course.getInstructor().getId().equals(requestingUserId);
        if (!isAdmin && !isInstructor) {
            throw new UnauthorizedActionException("Bu işlem için yetkiniz yok.");
        }
    }

    public boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    public boolean isInstructorOfCourse(Course course, Long userId) {
        return course.getInstructor() != null && course.getInstructor().getId().equals(userId);
    }
}
