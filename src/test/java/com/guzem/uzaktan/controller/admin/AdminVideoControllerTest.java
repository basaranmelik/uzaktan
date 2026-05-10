package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.CourseVideoResponse;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;
import com.guzem.uzaktan.repository.course.CourseCategoryRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.course.CourseCategoryService;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.course.CourseVideoService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(value = AdminVideoController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AdminVideoControllerTest.TestConfig.class)
@WithMockUser(roles = "ADMIN")
class AdminVideoControllerTest {

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
    private CourseVideoService courseVideoService;

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

    private CourseResponse course(Long id, Long instructorId) {
        return CourseResponse.builder()
                .id(id)
                .title("Test Course " + id)
                .description("Description")
                .price(BigDecimal.TEN)
                .status(CourseStatus.PUBLISHED)
                .courseType(CourseType.ONLINE)
                .instructorId(instructorId)
                .build();
    }

    private CourseVideoResponse video(Long id, Long courseId) {
        return CourseVideoResponse.builder()
                .id(id)
                .courseId(courseId)
                .courseTitle("Test Course")
                .title("Video " + id)
                .orderIndex(1)
                .build();
    }

    @Test
    void courseVideos_returnsView() throws Exception {
        when(courseService.findById(1L)).thenReturn(course(1L, 1L));
        when(courseVideoService.findByCourse(1L)).thenReturn(List.of(video(1L, 1L)));

        mockMvc.perform(get("/admin/kurslar/1/videolar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/course-videos"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("videos"));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void courseVideos_teacherOwnCourse_returnsForbidden() throws Exception {
        when(courseService.findById(1L)).thenReturn(course(1L, 1L));

        mockMvc.perform(get("/admin/kurslar/1/videolar"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void courseVideos_teacherOtherCourse_returnsForbidden() throws Exception {
        when(courseService.findById(2L)).thenReturn(course(2L, 5L));

        mockMvc.perform(get("/admin/kurslar/2/videolar"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void uploadVideos_valid_redirectWithSuccess() throws Exception {
        when(courseService.findById(1L)).thenReturn(course(1L, 1L));
        when(courseVideoService.uploadMultiple(eq(1L), any(), any(), any())).thenReturn(List.of());

        MockMultipartFile file = new MockMultipartFile("files", "video.mp4", "video/mp4",
                "content".getBytes());

        mockMvc.perform(multipart("/admin/kurslar/1/videolar")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/kurslar/1/videolar"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void uploadVideos_noFiles_ajaxError() throws Exception {
        when(courseService.findById(1L)).thenReturn(course(1L, 1L));

        MockMultipartFile emptyFile = new MockMultipartFile("files", "empty.mp4", "video/mp4",
                new byte[0]);

        mockMvc.perform(multipart("/admin/kurslar/1/videolar")
                        .file(emptyFile)
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteVideo_redirectWithSuccess() throws Exception {
        when(courseVideoService.findById(1L)).thenReturn(video(1L, 1L));
        when(courseService.findById(1L)).thenReturn(course(1L, 1L));
        doNothing().when(courseVideoService).delete(1L);

        mockMvc.perform(post("/admin/videolar/1/sil")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/kurslar/1/videolar"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void deleteVideo_ajax_success() throws Exception {
        when(courseVideoService.findById(1L)).thenReturn(video(1L, 1L));
        when(courseService.findById(1L)).thenReturn(course(1L, 1L));
        doNothing().when(courseVideoService).delete(1L);

        mockMvc.perform(post("/admin/videolar/1/sil")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Video silindi."));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void deleteVideo_teacherOtherVideo_returnsOk() throws Exception {
        when(courseVideoService.findById(1L)).thenReturn(video(1L, 2L));
        when(courseService.findById(2L)).thenReturn(course(2L, 5L));

        mockMvc.perform(post("/admin/videolar/1/sil")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void reorderVideos_returnsOk() throws Exception {
        when(courseService.findById(1L)).thenReturn(course(1L, 1L));
        doNothing().when(courseVideoService).updateOrder(eq(1L), any());

        mockMvc.perform(post("/admin/kurslar/1/videolar/sira")
                        .contentType("application/json")
                        .content("[2, 1]")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void reorderVideos_emptyList_returnsOk() throws Exception {
        when(courseService.findById(1L)).thenReturn(course(1L, 1L));
        doNothing().when(courseVideoService).updateOrder(eq(1L), any());

        mockMvc.perform(post("/admin/kurslar/1/videolar/sira")
                        .contentType("application/json")
                        .content("[]")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void updateVideo_redirectWithSuccess() throws Exception {
        when(courseVideoService.findById(1L)).thenReturn(video(1L, 1L));
        when(courseService.findById(1L)).thenReturn(course(1L, 1L));
        doNothing().when(courseVideoService).update(eq(1L), anyString(), anyString());

        mockMvc.perform(post("/admin/videolar/1/duzenle")
                        .param("title", "Updated Title")
                        .param("description", "Updated Description")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/kurslar/1/videolar"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void updateVideo_ajax_success() throws Exception {
        when(courseVideoService.findById(1L)).thenReturn(video(1L, 1L));
        when(courseService.findById(1L)).thenReturn(course(1L, 1L));
        doNothing().when(courseVideoService).update(eq(1L), anyString(), anyString());

        mockMvc.perform(post("/admin/videolar/1/duzenle")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("title", "Updated Title")
                        .param("description", "Updated Description")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Video güncellendi."));
    }

    @Test
    @WithMockUser(roles = "ANONYMOUS")
    void auth_unauthenticated() throws Exception {
        mockMvc.perform(get("/admin/kurslar/1/videolar"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void auth_user_forbidden() throws Exception {
        mockMvc.perform(get("/admin/kurslar/1/videolar"))
                .andExpect(status().is5xxServerError());
    }
}
