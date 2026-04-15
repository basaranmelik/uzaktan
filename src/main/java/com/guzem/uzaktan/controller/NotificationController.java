package com.guzem.uzaktan.controller;

import com.guzem.uzaktan.service.NotificationService;
import com.guzem.uzaktan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bildirimler")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        model.addAttribute("notifications", notificationService.getAll(userId, page, 20));
        notificationService.markAllRead(userId);
        return "bildirimler";
    }

    @PostMapping("/{id}/oku")
    @ResponseBody
    public ResponseEntity<Void> markRead(@PathVariable Long id,
                                          @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findUserIdByEmail(principal.getUsername());
        notificationService.markRead(id, userId);
        return ResponseEntity.ok().build();
    }
}
