package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.dto.request.VideoProgressRequest;
import com.guzem.uzaktan.dto.response.CourseVideoResponse;
import com.guzem.uzaktan.exception.ResourceNotFoundException;
import com.guzem.uzaktan.service.CourseService;
import com.guzem.uzaktan.service.CourseVideoService;
import com.guzem.uzaktan.service.EnrollmentService;
import com.guzem.uzaktan.service.FileStorageService;
import com.guzem.uzaktan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/videolar")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class VideoController {

    private final CourseVideoService courseVideoService;
    private final EnrollmentService enrollmentService;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final CourseService courseService;

    @GetMapping("/{id}")
    public String watchVideo(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails principal,
                             HttpServletRequest request,
                             Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        CourseVideoResponse video = courseVideoService.findById(id);
        if (!enrollmentService.isActiveEnrollment(userId, video.getCourseId())) {
            throw new ResourceNotFoundException("Video", "id", id);
        }

        if (!courseVideoService.canAccessVideo(id, userId)) {
            return "redirect:/egitimler/" + video.getCourseId();
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
        model.addAttribute("course", courseService.findById(video.getCourseId()));
        model.addAttribute("enrollment", enrollmentService.findByUserAndCourse(userId, video.getCourseId()).orElse(null));
        model.addAttribute("allVideos", allVideos);
        model.addAttribute("prevVideo", currentIndex > 0 ? allVideos.get(currentIndex - 1) : null);
        model.addAttribute("nextVideo", currentIndex < allVideos.size() - 1 ? allVideos.get(currentIndex + 1) : null);
        model.addAttribute("alreadyWatched", currentVideo.isWatched());
        
        // Gerçek TCP bağlantı adresi — kullanıcı tarafından manipüle edilemez
        model.addAttribute("clientIp", request.getRemoteAddr());
        
        return "video/izle";
    }

    @GetMapping("/{id}/stream")
    @ResponseBody
    public ResponseEntity<ResourceRegion> streamVideo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) throws java.io.IOException {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        CourseVideoResponse video = courseVideoService.findById(id);

        if (!enrollmentService.isActiveEnrollment(userId, video.getCourseId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!courseVideoService.canAccessVideo(id, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Path filePath = fileStorageService.resolve(video.getFilePath());
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = detectContentType(video.getOriginalFileName());
            long fileLength = resource.contentLength();

            if (rangeHeader == null) {
                // Full file request
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileLength))
                        .body(new ResourceRegion(resource, 0, fileLength));
            }

            // Range request handling
            long start = 0, end = fileLength - 1;
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String range = rangeHeader.substring("bytes=".length());
                String[] parts = range.split("-");
                try {
                    if (!parts[0].isEmpty()) {
                        start = Long.parseLong(parts[0]);
                    }
                    if (parts.length > 1 && !parts[1].isEmpty()) {
                        end = Long.parseLong(parts[1]);
                    }
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().build();
                }
                start = Math.max(0, Math.min(start, fileLength - 1));
                end = Math.max(start, Math.min(end, fileLength - 1));
            }

            long contentLength = end - start + 1;
            ResourceRegion region = new ResourceRegion(resource, start, contentLength);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                    .body(region);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/izlendi")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markWatched(@PathVariable Long id,
                                                            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        CourseVideoResponse video = courseVideoService.findById(id);
        if (!enrollmentService.isActiveEnrollment(userId, video.getCourseId())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(Map.of("success", false));
        }
        if (!courseVideoService.canAccessVideo(id, userId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(Map.of("success", false));
        }

        courseVideoService.markWatched(id, userId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/{id}/progress")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recordProgress(@PathVariable Long id,
                                                               @AuthenticationPrincipal UserDetails principal,
                                                               @RequestBody VideoProgressRequest req) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        CourseVideoResponse video = courseVideoService.findById(id);
        if (!enrollmentService.isActiveEnrollment(userId, video.getCourseId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("ok", false));
        }
        courseVideoService.recordProgress(id, userId, req);
        return ResponseEntity.ok(Map.of("ok", true));
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
