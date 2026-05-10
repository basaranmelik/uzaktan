package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.repository.course.CourseCategoryRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
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
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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

@WebMvcTest(value = AdminUserController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AdminUserControllerTest.TestConfig.class)
@WithMockUser(roles = "ADMIN", username = "admin@test.com")
class AdminUserControllerTest {

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
    private UserService userService;

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

    private UserResponse user(Long id, String email, Role role) {
        return UserResponse.builder()
                .id(id)
                .email(email)
                .firstName("Test")
                .lastName("User")
                .fullName("Test User")
                .role(role)
                .locked(false)
                .build();
    }

    @Test
    void listUsers_success() throws Exception {
        when(userService.findAllUsers()).thenReturn(List.of(user(1L, "a@test.com", Role.USER), user(2L, "b@test.com", Role.TEACHER)));

        mockMvc.perform(get("/admin/kullanicilar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("roles"));
    }

    @Test
    void toggleLock_ajax_success() throws Exception {
        when(userService.findUserIdByEmail("admin@test.com")).thenReturn(2L);
        doNothing().when(userService).toggleUserLock(1L);

        mockMvc.perform(post("/admin/kullanicilar/1/kilitle")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Kullanıcı durumu güncellendi."));

        verify(userService).toggleUserLock(1L);
    }

    @Test
    void toggleLock_ajax_selfLock() throws Exception {
        when(userService.findUserIdByEmail("admin@test.com")).thenReturn(1L);

        mockMvc.perform(post("/admin/kullanicilar/1/kilitle")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kendi hesabınızı kilitleyemezsiniz."));
    }

    @Test
    void toggleLock_nonAjax_redirect() throws Exception {
        when(userService.findUserIdByEmail("admin@test.com")).thenReturn(2L);
        doNothing().when(userService).toggleUserLock(1L);

        mockMvc.perform(post("/admin/kullanicilar/1/kilitle")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/kullanicilar"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).toggleUserLock(1L);
    }

    @Test
    void changeRole_ajax_success() throws Exception {
        when(userService.findUserIdByEmail("admin@test.com")).thenReturn(2L);
        doNothing().when(userService).changeRole(1L, Role.TEACHER);

        mockMvc.perform(post("/admin/kullanicilar/1/rol")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("role", "TEACHER")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rol güncellendi."));
    }

    @Test
    void changeRole_ajax_serviceException() throws Exception {
        when(userService.findUserIdByEmail("admin@test.com")).thenReturn(2L);
        doThrow(new RuntimeException("Rol değiştirme hatası")).when(userService).changeRole(1L, Role.TEACHER);

        mockMvc.perform(post("/admin/kullanicilar/1/rol")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("role", "TEACHER")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Rol değiştirme hatası"));
    }

    @Test
    void changeRole_nonAjax_redirect() throws Exception {
        when(userService.findUserIdByEmail("admin@test.com")).thenReturn(2L);
        doNothing().when(userService).changeRole(1L, Role.TEACHER);

        mockMvc.perform(post("/admin/kullanicilar/1/rol")
                        .param("role", "TEACHER")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/kullanicilar"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void delete_ajax_success() throws Exception {
        when(userService.findUserIdByEmail("admin@test.com")).thenReturn(2L);
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(post("/admin/kullanicilar/1/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Kullanıcı silindi."));

        verify(userService).deleteUser(1L);
    }

    @Test
    void delete_ajax_selfDelete() throws Exception {
        when(userService.findUserIdByEmail("admin@test.com")).thenReturn(1L);

        mockMvc.perform(post("/admin/kullanicilar/1/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kendi hesabınızı silemezsiniz."));
    }

    @Test
    void delete_ajax_notFound() throws Exception {
        when(userService.findUserIdByEmail("admin@test.com")).thenReturn(2L);
        doThrow(new RuntimeException("Kullanıcı bulunamadı")).when(userService).deleteUser(999L);

        mockMvc.perform(post("/admin/kullanicilar/999/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kullanıcı bulunamadı"));
    }

    @Test
    void delete_nonAjax_redirect() throws Exception {
        when(userService.findUserIdByEmail("admin@test.com")).thenReturn(2L);
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(post("/admin/kullanicilar/1/sil")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/kullanicilar"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "ANONYMOUS")
    void auth_unauthenticated() throws Exception {
        mockMvc.perform(get("/admin/kullanicilar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void auth_userRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/kullanicilar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void auth_teacherRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/kullanicilar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FIRM")
    void auth_firmRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/kullanicilar"))
                .andExpect(status().isOk());
    }
}
