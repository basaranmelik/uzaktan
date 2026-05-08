package com.guzem.uzaktan.controller.instructor;

import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/egitmenler")
@RequiredArgsConstructor
public class InstructorController {

    private final UserService userService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("instructors", userService.findUsersByRole(Role.TEACHER));
        return "egitmenler/liste";
    }
}
