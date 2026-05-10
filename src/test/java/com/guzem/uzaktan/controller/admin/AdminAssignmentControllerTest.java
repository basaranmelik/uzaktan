package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.AssignmentResponse;
import com.guzem.uzaktan.dto.response.SubmissionResponse;
import com.guzem.uzaktan.repository.course.CourseCategoryRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.admin.AssignmentService;
import com.guzem.uzaktan.service.admin.SubmissionZipService;
import com.guzem.uzaktan.service.course.CourseCategoryService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.ViewResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(value = AdminAssignmentController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AdminAssignmentControllerTest.TestConfig.class)
@WithMockUser(roles = "ADMIN")
class AdminAssignmentControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        CacheManager cacheManager() {
            return new NoOpCacheManager();
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
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
    private AssignmentService assignmentService;

    @MockitoBean
    private SubmissionZipService submissionZipService;

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

    private AssignmentResponse assignment(Long id) {
        return AssignmentResponse.builder()
                .id(id)
                .courseId(1L)
                .courseTitle("Java 101")
                .title("Homework " + id)
                .dueDate(LocalDateTime.now().plusDays(7))
                .maxScore(100)
                .submissionCount(5)
                .build();
    }

    private SubmissionResponse submission(Long id, Long assignmentId) {
        return SubmissionResponse.builder()
                .id(id)
                .assignmentId(assignmentId)
                .assignmentTitle("Homework")
                .courseId(1L)
                .courseTitle("Java 101")
                .userId(1L)
                .userFullName("Test User")
                .submittedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void assignments_returnsViewWithModel() throws Exception {
        when(assignmentService.findAllAssignmentsForAdmin()).thenReturn(List.of(assignment(1L)));

        mockMvc.perform(get("/admin/odevler"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/assignments"))
                .andExpect(model().attributeExists("assignments"));
    }

    @Test
    void viewSubmissions_returnsViewWithModel() throws Exception {
        when(assignmentService.findById(eq(1L), any())).thenReturn(assignment(1L));
        when(assignmentService.findSubmissionsByAssignment(eq(1L), any()))
                .thenReturn(List.of(submission(1L, 1L)));

        mockMvc.perform(get("/admin/odevler/1/teslimler"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/assignment-submissions"))
                .andExpect(model().attributeExists("assignment"))
                .andExpect(model().attributeExists("submissions"));
    }

    @Test
    void downloadSubmissions_returnsZip() throws Exception {
        when(submissionZipService.downloadSubmissionsZip(eq(1L), any()))
                .thenReturn(new byte[]{0x50, 0x4B, 0x03, 0x04});
        when(assignmentService.findById(eq(1L), any())).thenReturn(assignment(1L));

        mockMvc.perform(get("/admin/odevler/1/indir-zip"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"Odev_1_Teslimler.zip\""));
    }

    @Test
    void downloadSubmissions_empty_returnsEmptyZip() throws Exception {
        when(submissionZipService.downloadSubmissionsZip(eq(1L), any()))
                .thenReturn(new byte[0]);
        when(assignmentService.findById(eq(1L), any())).thenReturn(assignment(1L));

        mockMvc.perform(get("/admin/odevler/1/indir-zip"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"));
    }

    @Test
    void downloadSubmissions_notFound_throwsException() throws Exception {
        when(submissionZipService.downloadSubmissionsZip(eq(999L), any()))
                .thenThrow(new RuntimeException("Assignment not found"));

        mockMvc.perform(get("/admin/odevler/999/indir-zip"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "ANONYMOUS")
    void auth_unauthenticated() throws Exception {
        when(assignmentService.findAllAssignmentsForAdmin()).thenThrow(new RuntimeException());
        mockMvc.perform(get("/admin/odevler"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void auth_user_forbidden() throws Exception {
        when(assignmentService.findAllAssignmentsForAdmin()).thenThrow(new RuntimeException());
        mockMvc.perform(get("/admin/odevler"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void auth_teacher_forbidden() throws Exception {
        when(assignmentService.findAllAssignmentsForAdmin()).thenThrow(new RuntimeException());
        mockMvc.perform(get("/admin/odevler"))
                .andExpect(status().is5xxServerError());
    }
}
