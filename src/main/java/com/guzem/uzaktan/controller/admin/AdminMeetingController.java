package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.response.ZoomMeetingResponse;
import com.guzem.uzaktan.service.course.CourseService;
import com.guzem.uzaktan.service.instructor.ZoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/toplantilar")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMeetingController {

    private final ZoomService zoomService;
    private final CourseService courseService;

    @GetMapping
    public String listAllMeetings(Model model) {
        List<ZoomMeetingResponse> allMeetings = zoomService.findAllForAdmin();
        model.addAttribute("meetings", allMeetings);
        model.addAttribute("totalStarted", allMeetings.stream().filter(ZoomMeetingResponse::isHostJoined).count());
        model.addAttribute("totalMissed", allMeetings.stream()
                .filter(m -> !m.isHostJoined() && m.isPast()
                        && m.getStatus() != com.guzem.uzaktan.model.instructor.ZoomMeetingStatus.CANCELLED)
                .count());
        return "admin/meetings";
    }

    @GetMapping("/kurs/{courseId}")
    public String meetingsByCourse(@PathVariable Long courseId, Model model) {
        var course = courseService.findById(courseId);
        List<ZoomMeetingResponse> meetings = zoomService.findAllForAdminByCourse(courseId);
        model.addAttribute("course", course);
        model.addAttribute("meetings", meetings);
        model.addAttribute("totalStarted", meetings.stream().filter(ZoomMeetingResponse::isHostJoined).count());
        return "admin/meetings";
    }
}
