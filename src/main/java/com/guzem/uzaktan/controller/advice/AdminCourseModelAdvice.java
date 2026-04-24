package com.guzem.uzaktan.controller.advice;

import com.guzem.uzaktan.controller.admin.AdminCourseController;
import com.guzem.uzaktan.model.course.CourseLevel;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.service.course.CourseCategoryService;
import com.guzem.uzaktan.service.instructor.InstructorService;
import com.guzem.uzaktan.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * AdminCourseController'a özgü @ModelAttribute'lar.
 * <p>
 * Not: Bu değerler yalnızca assignableTypes ile belirtilen controller'ın
 * işlediği HTTP isteklerinde modele eklenir — diğer sayfalara yük bindirmez.
 */
@ControllerAdvice(assignableTypes = AdminCourseController.class)
@RequiredArgsConstructor
public class AdminCourseModelAdvice {

    private final CourseCategoryService categoryService;
    private final UserService userService;
    private final InstructorService instructorService;

    /** Kurs formu açılış seçenekleri — statik enum değerleri */
    @ModelAttribute("courseTypes")
    public CourseType[] courseTypes() {
        return CourseType.values();
    }

    @ModelAttribute("levels")
    public CourseLevel[] levels() {
        return CourseLevel.values();
    }

    @ModelAttribute("statuses")
    public CourseStatus[] statuses() {
        return CourseStatus.values();
    }

    /** Kategori listesi — admin kurs formlarında kullanılır */
    @ModelAttribute("categories")
    public List<?> categories() {
        return categoryService.findAll();
    }

    /** Eğitmen kullanıcı listesi (TEACHER rolü) */
    @ModelAttribute("teachers")
    public List<?> teachers() {
        return userService.findUsersByRole(Role.TEACHER);
    }

    /** Instructor profil listesi */
    @ModelAttribute("instructors")
    public List<?> instructors() {
        return instructorService.findAll();
    }
}
