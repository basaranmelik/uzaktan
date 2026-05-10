package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.repository.course.CourseCategoryRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.common.FileStorageService;
import com.guzem.uzaktan.service.course.CourseCategoryService;
import com.guzem.uzaktan.service.user.CartService;
import com.guzem.uzaktan.service.user.NotificationService;
import com.guzem.uzaktan.service.user.TeacherManagementService;
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
import org.springframework.mock.web.MockMultipartFile;
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
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AdminInstructorController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AdminInstructorControllerTest.TestConfig.class)
class AdminInstructorControllerTest {

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
    private TeacherManagementService teacherManagementService;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private CourseCategoryRepository courseCategoryRepository;

    @MockitoBean
    private CourseCategoryService courseCategoryService;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EntityManager entityManager;

    private UserResponse teacherResponse() {
        return UserResponse.builder()
                .id(1L)
                .email("ogretmen@example.com")
                .firstName("Ahmet")
                .lastName("Yılmaz")
                .fullName("Ahmet Yılmaz")
                .role(Role.TEACHER)
                .zoomEmail("ahmet.zoom@example.com")
                .build();
    }

    private UserResponse userResponse() {
        return UserResponse.builder()
                .id(2L)
                .email("kullanici@example.com")
                .firstName("Mehmet")
                .lastName("Demir")
                .fullName("Mehmet Demir")
                .role(Role.USER)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listInstructors_success() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of(teacherResponse()), PageRequest.of(0, 20), 1);
        when(userService.findUsersByRole(eq(Role.TEACHER), eq(PageRequest.of(0, 20)))).thenReturn(page);

        mockMvc.perform(get("/admin/egitmenler"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/instructors"))
                .andExpect(model().attribute("instructors", page.getContent()))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listInstructors_emptyPage() throws Exception {
        Page<UserResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(999, 20), 0);
        when(userService.findUsersByRole(eq(Role.TEACHER), eq(PageRequest.of(999, 20)))).thenReturn(emptyPage);

        mockMvc.perform(get("/admin/egitmenler").param("page", "999"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/instructors"))
                .andExpect(model().attribute("instructors", Collections.emptyList()))
                .andExpect(model().attribute("currentPage", 999))
                .andExpect(model().attribute("totalPages", 0))
                .andExpect(model().attribute("totalElements", 0L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void newInstructorForm_success() throws Exception {
        mockMvc.perform(get("/admin/egitmenler/yeni"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/instructor-form"))
                .andExpect(model().attributeExists("teacherRequest"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createInstructor_success() throws Exception {
        doNothing().when(teacherManagementService).createTeacher(any());

        mockMvc.perform(post("/admin/egitmenler")
                        .param("email", "yeni@example.com")
                        .param("firstName", "Ayşe")
                        .param("lastName", "Kaya")
                        .param("expertise", "Matematik")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/egitmenler"))
                .andExpect(flash().attribute("newTeacherEmail", "yeni@example.com"))
                .andExpect(flash().attribute("successMessage",
                        "Eğitmen başarıyla oluşturuldu. Şifre e-posta ile gönderildi."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createInstructor_validationError() throws Exception {
        mockMvc.perform(post("/admin/egitmenler")
                        .param("email", "")
                        .param("firstName", "")
                        .param("lastName", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/instructor-form"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createInstructor_duplicateEmail() throws Exception {
        doThrow(new IllegalArgumentException("Bu e-posta zaten kayıtlı."))
                .when(teacherManagementService).createTeacher(any());

        mockMvc.perform(post("/admin/egitmenler")
                        .param("email", "mevcut@example.com")
                        .param("firstName", "Ali")
                        .param("lastName", "Veli")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/egitmenler"))
                .andExpect(flash().attribute("errorMessage", "Bu e-posta zaten kayıtlı."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createInstructorAjax_success() throws Exception {
        when(teacherManagementService.createTeacherWithPassword(any())).thenReturn("tempPass123");

        mockMvc.perform(post("/admin/egitmenler/olustur-ajax")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("email", "ajax@example.com")
                        .param("firstName", "Can")
                        .param("lastName", "Ege")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("ajax@example.com"))
                .andExpect(jsonPath("$.data.password").value("tempPass123"))
                .andExpect(jsonPath("$.message").value("Eğitmen başarıyla oluşturuldu."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createInstructorAjax_duplicateEmail() throws Exception {
        when(teacherManagementService.createTeacherWithPassword(any()))
                .thenThrow(new IllegalArgumentException("Bu e-posta zaten kayıtlı."));

        mockMvc.perform(post("/admin/egitmenler/olustur-ajax")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("email", "mevcut@example.com")
                        .param("firstName", "Can")
                        .param("lastName", "Ege")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bu e-posta zaten kayıtlı."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editInstructorForm_success() throws Exception {
        UserResponse teacher = teacherResponse();
        when(userService.findById(1L)).thenReturn(teacher);

        mockMvc.perform(get("/admin/egitmenler/1/duzenle"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/instructor-edit"))
                .andExpect(model().attribute("teacher", teacher));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editInstructorForm_notTeacher() throws Exception {
        UserResponse regularUser = userResponse();
        when(userService.findById(2L)).thenReturn(regularUser);

        mockMvc.perform(get("/admin/egitmenler/2/duzenle"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/egitmenler"))
                .andExpect(flash().attribute("errorMessage", "Bu kullanıcı bir eğitmen değil."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateInstructor_success() throws Exception {
        UserResponse teacher = teacherResponse();
        when(userService.findById(1L)).thenReturn(teacher);
        when(userService.updateProfile(eq(1L), any())).thenReturn(teacher);

        mockMvc.perform(post("/admin/egitmenler/1")
                        .param("firstName", "Ahmet")
                        .param("lastName", "Güncel")
                        .param("email", "ogretmen@example.com")
                        .param("expertise", "Fizik")
                        .param("bio", "Yeni biyografi")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/egitmenler"))
                .andExpect(flash().attribute("successMessage", "Eğitmen bilgileri güncellendi."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteInstructor_ajax_success() throws Exception {
        UserResponse teacher = teacherResponse();
        when(userService.findById(1L)).thenReturn(teacher);
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(post("/admin/egitmenler/1/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Eğitmen silindi."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteInstructor_ajax_notTeacher() throws Exception {
        UserResponse regularUser = userResponse();
        when(userService.findById(2L)).thenReturn(regularUser);

        mockMvc.perform(post("/admin/egitmenler/2/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bu kullanıcı bir eğitmen değil."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteInstructor_ajax_notFound() throws Exception {
        when(userService.findById(999L)).thenThrow(new RuntimeException("Kullanıcı bulunamadı"));

        mockMvc.perform(post("/admin/egitmenler/999/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kullanıcı bulunamadı"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteInstructor_nonAjax_redirect() throws Exception {
        UserResponse teacher = teacherResponse();
        when(userService.findById(1L)).thenReturn(teacher);
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(post("/admin/egitmenler/1/sil")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/egitmenler"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void auth_unauthenticated() throws Exception {
        mockMvc.perform(get("/admin/egitmenler"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void auth_userRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/egitmenler"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void auth_teacherRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/egitmenler"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createInstructorAjax_withPhoto() throws Exception {
        when(teacherManagementService.createTeacherWithPassword(any())).thenReturn("photoPass456");
        when(userService.findUserIdByEmail("phototest@example.com")).thenReturn(1L);
        when(fileStorageService.storeImage(any())).thenReturn("profile_abc123.jpg");

        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "avatar.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/admin/egitmenler/olustur-ajax")
                        .file(photo)
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("email", "phototest@example.com")
                        .param("firstName", "Foto")
                        .param("lastName", "Test")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.password").value("photoPass456"));

        verify(userService).updateProfilePicture(eq(1L), eq("/uploads/profile_abc123.jpg"));
    }
}
