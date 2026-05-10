package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.CertificateResponse;
import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;
import com.guzem.uzaktan.repository.course.CourseCategoryRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.course.CertificateService;
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
import java.time.LocalDateTime;
import java.util.List;

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

@WebMvcTest(value = AdminCertificateController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AdminCertificateControllerTest.TestConfig.class)
@WithMockUser(roles = "ADMIN")
class AdminCertificateControllerTest {

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
    private CertificateService certificateService;

    @MockitoBean
    private UserService userService;

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
    private CartService cartService;

    @MockitoBean
    private NotificationService notificationService;

    private CertificateResponse cert(Long id, String courseTitle, String userName) {
        return CertificateResponse.builder()
                .id(id)
                .courseId(1L)
                .certificateCode("CERT-" + id)
                .courseTitle(courseTitle)
                .userName(userName)
                .issueDate(LocalDateTime.now())
                .build();
    }

    private UserResponse user(Long id, String email) {
        return UserResponse.builder()
                .id(id)
                .email(email)
                .firstName("Test")
                .lastName("User")
                .fullName("Test User")
                .role(Role.USER)
                .build();
    }

    private CourseResponse course(Long id, String title) {
        return CourseResponse.builder()
                .id(id)
                .title(title)
                .description("Description")
                .price(BigDecimal.TEN)
                .status(CourseStatus.PUBLISHED)
                .courseType(CourseType.ONLINE)
                .build();
    }

    @Test
    void listCertificates_success() throws Exception {
        when(certificateService.findAll()).thenReturn(List.of(cert(1L, "Java 101", "Ali Veli")));
        when(userService.findAllUsers()).thenReturn(List.of(user(1L, "ali@test.com")));
        when(courseService.findAllForAdmin(0, 200)).thenReturn(new PageImpl<>(List.of(course(1L, "Java 101")), PageRequest.of(0, 200), 1));

        mockMvc.perform(get("/admin/sertifikalar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/certificates"))
                .andExpect(model().attributeExists("certificates"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("courses"));
    }

    @Test
    void issueCertificate_success() throws Exception {
        when(certificateService.issueCertificate(1L, 1L)).thenReturn(cert(1L, "Java 101", "Ali Veli"));

        mockMvc.perform(post("/admin/sertifikalar/ver")
                        .param("userId", "1")
                        .param("courseId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/sertifikalar"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void issueCertificate_alreadyHasCert() throws Exception {
        when(certificateService.issueCertificate(1L, 1L))
                .thenThrow(new IllegalArgumentException("Bu kullanıcı zaten sertifikaya sahip."));

        mockMvc.perform(post("/admin/sertifikalar/ver")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("userId", "1")
                        .param("courseId", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bu kullanıcı zaten sertifikaya sahip."));
    }

    @Test
    void issueCertificate_invalidUser() throws Exception {
        when(certificateService.issueCertificate(999L, 1L))
                .thenThrow(new IllegalArgumentException("Kullanıcı bulunamadı."));

        mockMvc.perform(post("/admin/sertifikalar/ver")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("userId", "999")
                        .param("courseId", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kullanıcı bulunamadı."));
    }

    @Test
    void revoke_ajax_success() throws Exception {
        mockMvc.perform(post("/admin/sertifikalar/1/iptal")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sertifika iptal edildi."));
    }

    @Test
    void revoke_ajax_notFound() throws Exception {
        doThrow(new RuntimeException("Sertifika bulunamadı")).when(certificateService).revoke(999L);

        mockMvc.perform(post("/admin/sertifikalar/999/iptal")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Sertifika bulunamadı"));
    }

    @Test
    void revoke_nonAjax_redirect() throws Exception {
        mockMvc.perform(post("/admin/sertifikalar/1/iptal")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/sertifikalar"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(roles = "ANONYMOUS")
    void auth_unauthenticated() throws Exception {
        mockMvc.perform(get("/admin/sertifikalar"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void auth_userRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/sertifikalar"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void auth_teacherRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/sertifikalar"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void issueCertificate_courseIdZero_boundary() throws Exception {
        when(certificateService.issueCertificate(1L, 0L)).thenReturn(cert(1L, "Unknown", "Ali Veli"));

        mockMvc.perform(post("/admin/sertifikalar/ver")
                        .param("userId", "1")
                        .param("courseId", "0")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/sertifikalar"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}
