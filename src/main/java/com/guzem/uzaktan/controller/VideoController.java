package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.dto.response.CourseVideoResponse;
import com.guzem.uzaktan.exception.UnauthorizedActionException;
import com.guzem.uzaktan.service.CourseVideoService;
import com.guzem.uzaktan.service.EnrollmentService;
import com.guzem.uzaktan.service.FileStorageService;
import com.guzem.uzaktan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/videolar")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class VideoController {

    private final CourseVideoService courseVideoService;
    private final EnrollmentService enrollmentService;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    @GetMapping("/{id}")
    public String watchVideo(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails principal,
                             Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        CourseVideoResponse video = courseVideoService.findById(id);
        if (!enrollmentService.isActiveEnrollment(userId, video.getCourseId())) {
            throw new UnauthorizedActionException(
                    "Bu videoya erişmek için kursa kayıtlı olmanız ve kaydınızın onaylanmış (aktif) olması gerekiyor.");
        }

        List<CourseVideoResponse> allVideos = courseVideoService.findByCourseForStudent(video.getCourseId(), userId);

        int currentIndex = 0;
        CourseVideoResponse currentVideo = video;
        for (int i = 0; i < allVideos.size(); i++) {
            if (allVideos.get(i).getId().equals(id)) {
                currentIndex = i;
                currentVideo = allVideos.get(i);
                break;
            }
        }

        model.addAttribute("video", currentVideo);
        model.addAttribute("allVideos", allVideos);
        model.addAttribute("prevVideo", currentIndex > 0 ? allVideos.get(currentIndex - 1) : null);
        model.addAttribute("nextVideo", currentIndex < allVideos.size() - 1 ? allVideos.get(currentIndex + 1) : null);
        model.addAttribute("alreadyWatched", currentVideo.isWatched());
        return "video/izle";
    }

    @GetMapping("/{id}/stream")
    @ResponseBody
    public ResponseEntity<Resource> streamVideo(@PathVariable Long id,
                                                @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        CourseVideoResponse video = courseVideoService.findById(id);

        if (!enrollmentService.isActiveEnrollment(userId, video.getCourseId())) {
            return ResponseEntity.status(403).build();
        }

        Path filePath = fileStorageService.resolve(video.getFilePath());
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(detectContentType(video.getOriginalFileName())))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/izlendi")
    public String markWatched(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails principal,
                              RedirectAttributes redirectAttributes) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        CourseVideoResponse video = courseVideoService.findById(id);
        if (!enrollmentService.isActiveEnrollment(userId, video.getCourseId())) {
            throw new UnauthorizedActionException("Bu kursa kaydınız aktif değil.");
        }

        courseVideoService.markWatched(id, userId);
        redirectAttributes.addFlashAttribute("successMessage", "Video tamamlandı olarak işaretlendi.");
        return "redirect:/videolar/" + id;
    }

    private String detectContentType(String fileName) {
        if (fileName == null) return "video/mp4";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".ogg") || lower.endsWith(".ogv")) return "video/ogg";
        if (lower.endsWith(".mov")) return "video/quicktime";
        if (lower.endsWith(".avi")) return "video/x-msvideo";
        return "video/mp4";
    }
}
