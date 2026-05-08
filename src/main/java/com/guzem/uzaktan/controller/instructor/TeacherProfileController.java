package com.guzem.uzaktan.controller.instructor;

import com.guzem.uzaktan.dto.request.ProfileUpdateRequest;
import com.guzem.uzaktan.dto.request.TeacherProfileUpdateRequest;
import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.security.CustomUserDetails;
import com.guzem.uzaktan.service.common.FileStorageService;
import com.guzem.uzaktan.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/egitmen/profilim")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class TeacherProfileController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String showProfile(@AuthenticationPrincipal CustomUserDetails details, Model model) {
        UserResponse user = userService.findById(details.getUserId());
        model.addAttribute("teacher", user);
        TeacherProfileUpdateRequest form = new TeacherProfileUpdateRequest();
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setBio(user.getBio());
        form.setSkills(user.getSkillsList() != null && !user.getSkillsList().isEmpty()
                ? String.join(", ", user.getSkillsList()) : "");
        model.addAttribute("profileForm", form);
        return "egitmen/profilim";
    }

    @PostMapping
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails details,
                                @Valid @ModelAttribute("profileForm") TeacherProfileUpdateRequest request,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("teacher", userService.findById(details.getUserId()));
            return "egitmen/profilim";
        }

        Long userId = details.getUserId();

        // Update first/last name via existing updateProfile
        ProfileUpdateRequest nameReq = ProfileUpdateRequest.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
        userService.updateProfile(userId, nameReq);

        // Update bio and skills
        userService.updateTeacherFields(userId, request.getBio(), request.getSkills());

        // Handle optional profile picture upload
        MultipartFile photo = request.getProfilePicture();
        if (photo != null && !photo.isEmpty()) {
            try {
                String url = "/uploads/" + fileStorageService.storeImage(photo);
                userService.updateProfilePicture(userId, url);
            } catch (IllegalArgumentException e) {
                model.addAttribute("teacher", userService.findById(userId));
                model.addAttribute("profileForm", request);
                model.addAttribute("errorMessage", e.getMessage());
                return "egitmen/profilim";
            } catch (java.io.IOException e) {
                model.addAttribute("teacher", userService.findById(userId));
                model.addAttribute("profileForm", request);
                model.addAttribute("errorMessage", "Fotoğraf yüklenirken bir hata oluştu.");
                return "egitmen/profilim";
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Profil bilgileri güncellendi.");
        return "redirect:/egitmen/profilim";
    }
}
