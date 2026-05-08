package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.service.admin.DashboardStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final DashboardStatsService dashboardStatsService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAllAttributes(dashboardStatsService.getDashboardStats());
        return "admin/dashboard";
    }
}
