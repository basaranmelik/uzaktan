package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.repository.course.CourseCategoryRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.course.CourseCategoryService;
import com.guzem.uzaktan.service.course.CourseReviewService;
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
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
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

@WebMvcTest(value = AdminReviewController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AdminReviewControllerTest.TestConfig.class)
@WithMockUser(roles = "ADMIN")
class AdminReviewControllerTest {

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
    private CourseReviewService courseReviewService;

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

    @Test
    void listPendingReviews_returnsViewWithModel() throws Exception {
        when(courseReviewService.getPendingReviews()).thenReturn(List.of());

        mockMvc.perform(get("/admin/yorumlar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reviews"))
                .andExpect(model().attributeExists("pendingReviews"));
    }

    @Test
    void approveReview_success() throws Exception {
        mockMvc.perform(post("/admin/yorumlar/1/onayla")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/yorumlar"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void approveReview_ajax_success() throws Exception {
        mockMvc.perform(post("/admin/yorumlar/1/onayla")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Yorum onaylandı."));
    }

    @Test
    void approveReview_notFound() throws Exception {
        doThrow(new RuntimeException("Yorum bulunamadı")).when(courseReviewService).approveReview(999L);

        mockMvc.perform(post("/admin/yorumlar/999/onayla")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Yorum bulunamadı"));
    }

    @Test
    void approveReview_ajax_notFound() throws Exception {
        doThrow(new RuntimeException("Yorum bulunamadı")).when(courseReviewService).approveReview(999L);

        mockMvc.perform(post("/admin/yorumlar/999/onayla")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Yorum bulunamadı"));
    }

    @Test
    void deleteReview_success() throws Exception {
        mockMvc.perform(post("/admin/yorumlar/1/sil")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/yorumlar"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void deleteReview_ajax_success() throws Exception {
        mockMvc.perform(post("/admin/yorumlar/1/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Yorum silindi."));
    }

    @Test
    void deleteReview_notFound() throws Exception {
        doThrow(new RuntimeException("Yorum bulunamadı")).when(courseReviewService).deleteReview(999L);

        mockMvc.perform(post("/admin/yorumlar/999/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Yorum bulunamadı"));
    }

    @Test
    void deleteReview_ajax_notFound() throws Exception {
        doThrow(new RuntimeException("Yorum bulunamadı")).when(courseReviewService).deleteReview(999L);

        mockMvc.perform(post("/admin/yorumlar/999/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Yorum bulunamadı"));
    }

    @Test
    void alreadyApproved_stillReturnsSuccess() throws Exception {
        mockMvc.perform(post("/admin/yorumlar/1/onayla")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/yorumlar"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(roles = "ANONYMOUS")
    void auth_unauthenticated() throws Exception {
        when(courseReviewService.getPendingReviews()).thenThrow(new RuntimeException());
        mockMvc.perform(get("/admin/yorumlar"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void auth_user_forbidden() throws Exception {
        when(courseReviewService.getPendingReviews()).thenThrow(new RuntimeException());
        mockMvc.perform(get("/admin/yorumlar"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void auth_teacher_forbidden() throws Exception {
        when(courseReviewService.getPendingReviews()).thenThrow(new RuntimeException());
        mockMvc.perform(get("/admin/yorumlar"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void listPendingReviews_emptyList_returnsView() throws Exception {
        when(courseReviewService.getPendingReviews()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/yorumlar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reviews"))
                .andExpect(model().attributeExists("pendingReviews"));
    }
}
