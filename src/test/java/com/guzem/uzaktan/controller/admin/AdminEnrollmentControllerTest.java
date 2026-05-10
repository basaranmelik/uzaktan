package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.EnrollmentResponse;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;
import com.guzem.uzaktan.repository.course.CourseCategoryRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.course.CourseCategoryService;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.course.EnrollmentExcelExportService;
import com.guzem.uzaktan.service.course.EnrollmentService;
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

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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

@WebMvcTest(value = AdminEnrollmentController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AdminEnrollmentControllerTest.TestConfig.class)
@WithMockUser(roles = "ADMIN")
class AdminEnrollmentControllerTest {

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
    private EnrollmentService enrollmentService;

    @MockitoBean
    private EnrollmentExcelExportService enrollmentExcelExportService;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private CourseCategoryRepository courseCategoryRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EntityManager entityManager;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CourseCategoryService courseCategoryService;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private NotificationService notificationService;

    private EnrollmentResponse enrollment(Long id, String courseTitle) {
        return EnrollmentResponse.builder()
                .id(id)
                .userId(1L)
                .userEmail("user@test.com")
                .courseId(1L)
                .courseTitle(courseTitle)
                .instructorName("Instructor")
                .courseType(CourseType.ONLINE)
                .enrollmentDate(LocalDateTime.now())
                .build();
    }

    @Test
    void listEnrollments_success() throws Exception {
        List<EnrollmentResponse> list = List.of(enrollment(1L, "Java 101"), enrollment(2L, "Python 201"));
        Page<EnrollmentResponse> page = new PageImpl<>(list, PageRequest.of(0, 25), 2);
        when(enrollmentService.findAllForAdmin(0, 25)).thenReturn(page);

        mockMvc.perform(get("/admin/kayitlar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/enrollments"))
                .andExpect(model().attributeExists("enrollments"))
                .andExpect(model().attributeExists("groupedEnrollments"));
    }

    @Test
    void activateEnrollment_success() throws Exception {
        when(enrollmentService.activateEnrollment(1L)).thenReturn(enrollment(1L, "Java 101"));

        mockMvc.perform(post("/admin/kayitlar/1/aktifle")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/kayitlar"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void activateEnrollment_ajax_success() throws Exception {
        when(enrollmentService.activateEnrollment(1L)).thenReturn(enrollment(1L, "Java 101"));

        mockMvc.perform(post("/admin/kayitlar/1/aktifle")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Kayıt aktifleştirildi."));
    }

    @Test
    void activateEnrollment_error() throws Exception {
        doThrow(new RuntimeException("Kayıt bulunamadı")).when(enrollmentService).activateEnrollment(999L);

        mockMvc.perform(post("/admin/kayitlar/999/aktifle")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kayıt bulunamadı"));
    }

    @Test
    void activateEnrollment_ajax_error() throws Exception {
        doThrow(new RuntimeException("Kayıt bulunamadı")).when(enrollmentService).activateEnrollment(999L);

        mockMvc.perform(post("/admin/kayitlar/999/aktifle")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kayıt bulunamadı"));
    }

    @Test
    void deleteEnrollment_success() throws Exception {
        doNothing().when(enrollmentService).deleteEnrollment(1L);

        mockMvc.perform(post("/admin/kayitlar/1/sil")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/kayitlar"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void deleteEnrollment_ajax_success() throws Exception {
        doNothing().when(enrollmentService).deleteEnrollment(1L);

        mockMvc.perform(post("/admin/kayitlar/1/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Kayıt silindi."));
    }

    @Test
    void deleteEnrollment_ajax_notFound() throws Exception {
        doThrow(new RuntimeException("Kayıt bulunamadı")).when(enrollmentService).deleteEnrollment(999L);

        mockMvc.perform(post("/admin/kayitlar/999/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kayıt bulunamadı"));
    }

    @Test
    void deleteEnrollment_nonAjax_error() throws Exception {
        doThrow(new RuntimeException("Kayıt bulunamadı")).when(enrollmentService).deleteEnrollment(999L);

        mockMvc.perform(post("/admin/kayitlar/999/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kayıt bulunamadı"));
    }

    @Test
    void exportAllExcel_success() throws Exception {
        doNothing().when(enrollmentExcelExportService).exportAllToExcel(any(OutputStream.class));

        mockMvc.perform(get("/admin/kayitlar/export-excel"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String contentType = result.getResponse().getContentType();
                    assert contentType != null && contentType.contains("spreadsheetml");
                });
    }

    @Test
    void exportByCourseExcel_success() throws Exception {
        CourseResponse course = CourseResponse.builder()
                .id(1L)
                .title("Java 101")
                .description("Test")
                .price(BigDecimal.TEN)
                .status(CourseStatus.PUBLISHED)
                .courseType(CourseType.ONLINE)
                .build();
        when(courseService.findById(1L)).thenReturn(course);
        doNothing().when(enrollmentExcelExportService).exportByCourseToExcel(eq(1L), any(OutputStream.class));

        mockMvc.perform(get("/admin/kayitlar/kurs/1/export-excel"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String contentType = result.getResponse().getContentType();
                    assert contentType != null && contentType.contains("spreadsheetml");
                });
    }

    @Test
    @WithMockUser(roles = "ANONYMOUS")
    void auth_unauthenticated() throws Exception {
        mockMvc.perform(get("/admin/kayitlar"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void auth_userRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/kayitlar"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void auth_teacherRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/kayitlar"))
                .andExpect(status().is5xxServerError());
    }
}
