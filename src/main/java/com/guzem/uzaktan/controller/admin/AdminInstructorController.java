package com.guzem.uzaktan.controller.admin;

import com.guzem.uzaktan.dto.request.AdminInstructorUpdateRequest;
import com.guzem.uzaktan.dto.request.ProfileUpdateRequest;
import com.guzem.uzaktan.dto.request.TeacherCreateRequest;
import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.model.common.Role;
import com.guzem.uzaktan.service.common.FileStorageService;
import com.guzem.uzaktan.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/admin/egitmenler")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminInstructorController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String listInstructors(Model model) {
        model.addAttribute("instructors", userService.findUsersByRole(Role.TEACHER));
        return "admin/instructors";
    }

    @GetMapping("/yeni")
    public String createForm(Model model) {
        model.addAttribute("teacherRequest", new TeacherCreateRequest());
        return "admin/instructor-form";
    }

    @PostMapping
    public String createInstructor(@Valid @ModelAttribute("teacherRequest") TeacherCreateRequest request,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/instructor-form";
        }
        try {
            String generatedPassword = userService.createTeacher(request);
            redirectAttributes.addFlashAttribute("newTeacherEmail", request.getEmail());
            redirectAttributes.addFlashAttribute("newTeacherPassword", generatedPassword);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/egitmenler";
    }

    @GetMapping("/{id}/duzenle")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        UserResponse teacher = userService.findById(id);
        if (teacher.getRole() != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bu kullanıcı bir eğitmen değil.");
            return "redirect:/admin/egitmenler";
        }
        model.addAttribute("teacher", teacher);
        return "admin/instructor-edit";
    }

    @PostMapping("/{id}")
    public String updateInstructor(@PathVariable Long id,
                                    @Valid @ModelAttribute AdminInstructorUpdateRequest request,
                                    BindingResult result,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        UserResponse teacher = userService.findById(id);
        if (teacher.getRole() != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bu kullanıcı bir eğitmen değil.");
            return "redirect:/admin/egitmenler";
        }

        if (result.hasErrors()) {
            model.addAttribute("teacher", teacher);
            return "admin/instructor-edit";
        }

        ProfileUpdateRequest profileReq = ProfileUpdateRequest.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhone())
                .birthDate(request.getBirthDate())
                .city(request.getCity())
                .district(request.getDistrict())
                .zipCode(request.getZipCode())
                .fullAddress(request.getFullAddress())
                .zoomEmail(request.getZoomEmail())
                .build();
        userService.updateProfile(id, profileReq);
        userService.updateTeacherFields(id, request.getBio(), request.getExpertise());

        MultipartFile photo = request.getPhoto();
        if (photo != null && !photo.isEmpty()) {
            try {
                String url = "/uploads/" + fileStorageService.storeImage(photo);
                userService.updateProfilePicture(id, url);
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/admin/egitmenler/" + id + "/duzenle";
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Fotoğraf yüklenirken bir hata oluştu.");
                return "redirect:/admin/egitmenler/" + id + "/duzenle";
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Eğitmen bilgileri güncellendi.");
        return "redirect:/admin/egitmenler";
    }

    @PostMapping("/{id}/sil")
    public String deleteInstructor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        UserResponse teacher = userService.findById(id);
        if (teacher.getRole() != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bu kullanıcı bir eğitmen değil.");
            return "redirect:/admin/egitmenler";
        }
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Eğitmen silindi.");
        return "redirect:/admin/egitmenler";
    }
}
