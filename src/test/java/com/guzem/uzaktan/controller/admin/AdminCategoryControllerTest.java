package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.model.course.CourseCategory;
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

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyString;
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

@WebMvcTest(value = AdminCategoryController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AdminCategoryControllerTest.TestConfig.class)
@WithMockUser(roles = "ADMIN")
class AdminCategoryControllerTest {

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
    private CourseCategoryService categoryService;

    @MockitoBean
    private CourseCategoryRepository courseCategoryRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EntityManager entityManager;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private NotificationService notificationService;

    private CourseCategory category(Long id, String displayName) {
        return CourseCategory.builder().id(id).displayName(displayName).build();
    }

    @Test
    void listCategories_success() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(category(1L, "Yazılım"), category(2L, "Tasarım")));

        mockMvc.perform(get("/admin/kategoriler"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void createCategory_success() throws Exception {
        when(categoryService.create("Yazılım")).thenReturn(category(1L, "Yazılım"));

        mockMvc.perform(post("/admin/kategoriler")
                        .param("displayName", "Yazılım")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/kategoriler"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void createCategory_duplicateName() throws Exception {
        when(categoryService.create("Yazılım"))
                .thenThrow(new IllegalArgumentException("Bu kategori zaten mevcut."));

        mockMvc.perform(post("/admin/kategoriler")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("displayName", "Yazılım")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bu kategori zaten mevcut."));
    }

    @Test
    void createCategory_emptyDisplayName() throws Exception {
        when(categoryService.create(""))
                .thenThrow(new IllegalArgumentException("Kategori adı boş olamaz."));

        mockMvc.perform(post("/admin/kategoriler")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("displayName", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kategori adı boş olamaz."));
    }

    @Test
    void delete_ajax_success() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(post("/admin/kategoriler/1/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void delete_ajax_serviceError() throws Exception {
        doThrow(new RuntimeException("Silme hatası")).when(categoryService).delete(1L);

        mockMvc.perform(post("/admin/kategoriler/1/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void delete_ajax_categoryInUse() throws Exception {
        doThrow(new RuntimeException("Bu kategori kullanımda olduğu için silinemez."))
                .when(categoryService).delete(1L);

        mockMvc.perform(post("/admin/kategoriler/1/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void delete_nonAjax_redirect() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(post("/admin/kategoriler/1/sil")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/kategoriler"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(roles = "ANONYMOUS")
    void auth_unauthenticated() throws Exception {
        mockMvc.perform(get("/admin/kategoriler"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void auth_userRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/kategoriler"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void auth_teacherRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/kategoriler"))
                .andExpect(status().isOk());
    }
}
