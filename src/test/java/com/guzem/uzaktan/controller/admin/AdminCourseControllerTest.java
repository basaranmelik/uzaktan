package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.InstructorResponse;
import com.guzem.uzaktan.model.course.CourseLevel;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;
import com.guzem.uzaktan.repository.course.CourseCategoryRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.course.CourseCategoryService;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.user.CartService;
import com.guzem.uzaktan.service.user.NotificationService;
import com.guzem.uzaktan.service.user.UserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(value = AdminCourseController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AdminCourseControllerTest.TestConfig.class)
@WithMockUser(roles = "ADMIN")
class AdminCourseControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        CacheManager cacheManager() {
            return new NoOpCacheManager();
        }

        @Bean
        ViewResolver viewResolver() {
            return (viewName, locale) -> {
                if (viewName.startsWith("redirect:") || viewName.startsWith("forward:")) {
                    return null;
                }
                return (model, request, response) -> {};
            };
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        WebMvcConfigurer webMvcConfigurer() {
            return new WebMvcConfigurer() {
                @Override
                public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                    resolvers.add(new AuthenticationPrincipalArgumentResolver());
                }
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private CourseCategoryRepository courseCategoryRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EntityManager entityManager;

    @MockitoBean
    private CourseCategoryService courseCategoryService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private NotificationService notificationService;

    private CourseResponse buildCourseResponse(Long id, String title) {
        return CourseResponse.builder()
                .id(id)
                .title(title)
                .description("Description for " + title)
                .price(new BigDecimal("99.99"))
                .quota(30)
                .enrolledCount(10)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 7, 1))
                .hours(40)
                .status(CourseStatus.PUBLISHED)
                .level(CourseLevel.BEGINNER)
                .levelDisplayName("Başlangıç Seviye")
                .courseType(CourseType.ONLINE)
                .courseTypeDisplayName("Online")
                .instructorName("Test Instructor")
                .instructorId(1L)
                .instructors(null)
                .featured(false)
                .build();
    }

    private CourseResponse buildCourseWithInstructors(Long id) {
        InstructorResponse instructor = InstructorResponse.builder()
                .id(1L)
                .name("Test Instructor")
                .build();
        return CourseResponse.builder()
                .id(id)
                .title("Course " + id)
                .description("Description " + id)
                .price(new BigDecimal("99.99"))
                .quota(30)
                .enrolledCount(10)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 7, 1))
                .hours(40)
                .status(CourseStatus.PUBLISHED)
                .level(CourseLevel.BEGINNER)
                .levelDisplayName("Başlangıç Seviye")
                .courseType(CourseType.ONLINE)
                .courseTypeDisplayName("Online")
                .instructorName("Test Instructor")
                .instructorId(1L)
                .instructors(List.of(instructor))
                .featured(false)
                .build();
    }

    @Test
    void listCourses_success() throws Exception {
        List<CourseResponse> courses = List.of(
                buildCourseResponse(1L, "Java 101"),
                buildCourseResponse(2L, "Python 201")
        );
        Page<CourseResponse> page = new PageImpl<>(courses, PageRequest.of(0, 20), 2);
        when(courseService.findAllForAdmin(0, 20)).thenReturn(page);

        mockMvc.perform(get("/admin/kurslar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/courses"))
                .andExpect(model().attributeExists("courses"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attribute("courses", page));
    }

    @Test
    void listCourses_emptyPage() throws Exception {
        Page<CourseResponse> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(999, 20), 0);
        when(courseService.findAllForAdmin(999, 20)).thenReturn(page);

        mockMvc.perform(get("/admin/kurslar").param("page", "999"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/courses"))
                .andExpect(model().attributeExists("courses"))
                .andExpect(model().attribute("courses", page));
    }

    @Test
    void newCourseForm_success() throws Exception {
        mockMvc.perform(get("/admin/kurslar/yeni"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/course-form"))
                .andExpect(model().attributeExists("courseCreateRequest"));
    }

    @Test
    void createCourse_success() throws Exception {
        mockMvc.perform(post("/admin/kurslar")
                        .param("courseType", "ONLINE")
                        .param("title", "Test Course")
                        .param("description", "Test Description")
                        .param("price", "99.99")
                        .param("category.id", "1")
                        .param("category.displayName", "Test Category")
                        .param("instructorId", "1")
                        .param("currentUserId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/kurslar"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(courseService).create(any(), eq(null), eq(null));
    }

    @Test
    void createCourse_validationError() throws Exception {
        mockMvc.perform(post("/admin/kurslar")
                        .param("title", "")
                        .param("description", "")
                        .param("currentUserId", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/course-form"))
                .andExpect(model().attributeHasErrors("courseCreateRequest"));
    }

    @Test
    void createCourse_serviceError() throws Exception {
        when(courseService.create(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Kurs oluşturulamadı: geçersiz veri"));

        mockMvc.perform(post("/admin/kurslar")
                        .param("courseType", "ONLINE")
                        .param("title", "Test Course")
                        .param("description", "Test Description")
                        .param("price", "99.99")
                        .param("category.id", "1")
                        .param("category.displayName", "Test Category")
                        .param("instructorId", "1")
                        .param("currentUserId", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/course-form"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Kurs oluşturulamadı: geçersiz veri"));
    }

    @Test
    void editCourseForm_success() throws Exception {
        CourseResponse course = buildCourseWithInstructors(1L);
        when(courseService.findById(1L)).thenReturn(course);

        mockMvc.perform(get("/admin/kurslar/1/duzenle"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/course-edit"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("courseUpdateRequest"))
                .andExpect(model().attribute("course", course));
    }

    @Test
    void updateCourse_success() throws Exception {
        mockMvc.perform(post("/admin/kurslar/1")
                        .param("title", "Updated Course")
                        .param("description", "Updated Description")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/kurslar"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(courseService).update(eq(1L), any(), eq(null));
    }

    @Test
    void changeCourseStatus_success() throws Exception {
        mockMvc.perform(post("/admin/kurslar/1/durum")
                        .param("status", "PUBLISHED")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/kurslar"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(courseService).changeStatus(1L, CourseStatus.PUBLISHED);
    }

    @Test
    void changeCourseStatus_ajax() throws Exception {
        mockMvc.perform(post("/admin/kurslar/1/durum")
                        .param("status", "PUBLISHED")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Kurs durumu güncellendi."));

        verify(courseService).changeStatus(1L, CourseStatus.PUBLISHED);
    }

    @Test
    void deleteCourse_ajax_success() throws Exception {
        mockMvc.perform(post("/admin/kurslar/1/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Kurs iptal edildi."));

        verify(courseService).delete(1L);
    }

    @Test
    void deleteCourse_ajax_error() throws Exception {
        doThrow(new RuntimeException("Silme işlemi başarısız")).when(courseService).delete(1L);

        mockMvc.perform(post("/admin/kurslar/1/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Silme işlemi başarısız"));
    }

    @Test
    void deleteCourse_nonAjax_redirect() throws Exception {
        mockMvc.perform(post("/admin/kurslar/1/sil")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/kurslar"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(courseService).delete(1L);
    }

    @Test
    void toggleFeatured_success() throws Exception {
        when(courseService.toggleFeatured(1L)).thenReturn(true);

        mockMvc.perform(post("/admin/kurslar/1/onecikar")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Kurs ana sayfada gösterilecek."));
    }

    @Test
    void toggleFeatured_error() throws Exception {
        when(courseService.toggleFeatured(999L)).thenThrow(new IllegalArgumentException("Kurs bulunamadı"));

        mockMvc.perform(post("/admin/kurslar/999/onecikar")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kurs bulunamadı"));
    }

    @Test
    @WithMockUser(roles = "ANONYMOUS")
    void auth_unauthenticated() throws Exception {
        mockMvc.perform(get("/admin/kurslar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/courses"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void auth_userRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/kurslar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/courses"));
    }

    @Test
    @WithMockUser(roles = "FIRM")
    void auth_firmRole_allowed() throws Exception {
        Page<CourseResponse> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        when(courseService.findAllForAdmin(0, 20)).thenReturn(page);

        mockMvc.perform(get("/admin/kurslar"))
                .andExpect(status().isOk());
    }

    @Test
    void listCourses_pagination() throws Exception {
        List<CourseResponse> courses = IntStream.rangeClosed(1, 20)
                .mapToObj(i -> buildCourseResponse((long) i, "Course " + i))
                .collect(Collectors.toList());
        Page<CourseResponse> page = new PageImpl<>(courses, PageRequest.of(1, 20), 50);
        when(courseService.findAllForAdmin(1, 20)).thenReturn(page);

        mockMvc.perform(get("/admin/kurslar").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/courses"))
                .andExpect(model().attributeExists("courses"))
                .andExpect(model().attribute("courses", page));
    }

    @Test
    void updateCourse_validationError() throws Exception {
        CourseResponse course = buildCourseResponse(1L, "Test Course");
        when(courseService.findById(1L)).thenReturn(course);

        mockMvc.perform(post("/admin/kurslar/1")
                        .param("title", "ab")
                        .param("description", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/course-edit"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeHasErrors("courseUpdateRequest"));
    }
}
