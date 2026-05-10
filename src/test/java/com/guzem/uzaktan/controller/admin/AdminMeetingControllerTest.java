package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.CourseResponse;
import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.model.course.CourseStatus;
import com.guzem.uzaktan.model.course.CourseType;
import com.guzem.uzaktan.model.instructor.ZoomMeetingStatus;
import com.guzem.uzaktan.repository.course.CourseCategoryRepository;
import com.guzem.uzaktan.repository.user.UserRepository;
import com.guzem.uzaktan.service.course.CourseCategoryService;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.instructor.ZoomService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(value = AdminMeetingController.class, excludeAutoConfiguration = ThymeleafAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AdminMeetingControllerTest.TestConfig.class)
@WithMockUser(roles = "ADMIN")
class AdminMeetingControllerTest {

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
    private ZoomService zoomService;

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

    private ZoomMeetingResponse meeting(Long id, String topic, boolean hostJoined, boolean past) {
        return ZoomMeetingResponse.builder()
                .id(id)
                .topic(topic)
                .hostJoined(hostJoined)
                .past(past)
                .status(ZoomMeetingStatus.SCHEDULED)
                .courseId(1L)
                .courseTitle("Java 101")
                .scheduledAt(LocalDateTime.now().minusDays(1))
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
    void listAllMeetings_returnsViewWithModel() throws Exception {
        when(zoomService.findAllForAdmin()).thenReturn(List.of(
                meeting(1L, "Toplantı 1", true, true),
                meeting(2L, "Toplantı 2", false, true)));

        mockMvc.perform(get("/admin/toplantilar"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/meetings"))
                .andExpect(model().attributeExists("meetings"))
                .andExpect(model().attributeExists("totalStarted"))
                .andExpect(model().attributeExists("totalMissed"));
    }

    @Test
    void meetingsByCourse_returnsViewWithCourseMeetings() throws Exception {
        when(courseService.findById(1L)).thenReturn(course(1L, "Java 101"));
        when(zoomService.findAllForAdminByCourse(1L)).thenReturn(List.of(
                meeting(1L, "Kurs Toplantısı", true, true)));

        mockMvc.perform(get("/admin/toplantilar/kurs/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/meetings"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("meetings"))
                .andExpect(model().attributeExists("totalStarted"));
    }

    @Test
    void meetingsByCourse_nonExistentCourse_returnsViewWithEmptyMeetings() throws Exception {
        when(courseService.findById(999L)).thenReturn(course(999L, "Bilinmeyen Kurs"));
        when(zoomService.findAllForAdminByCourse(999L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/toplantilar/kurs/999"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/meetings"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("meetings"));
    }

    @Test
    @WithMockUser(roles = "ANONYMOUS")
    void auth_unauthenticated() throws Exception {
        when(zoomService.findAllForAdmin()).thenThrow(new RuntimeException());
        mockMvc.perform(get("/admin/toplantilar"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void auth_user_forbidden() throws Exception {
        when(zoomService.findAllForAdmin()).thenThrow(new RuntimeException());
        mockMvc.perform(get("/admin/toplantilar"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void auth_teacher_forbidden() throws Exception {
        when(zoomService.findAllForAdmin()).thenThrow(new RuntimeException());
        mockMvc.perform(get("/admin/toplantilar"))
                .andExpect(status().is5xxServerError());
    }
}
