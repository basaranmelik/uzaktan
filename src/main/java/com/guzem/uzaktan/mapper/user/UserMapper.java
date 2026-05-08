package com.guzem.uzaktan.mapper.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.dto.request.ProfileUpdateRequest;
import com.guzem.uzaktan.model.common.User;
import com.guzem.uzaktan.util.PhoneUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final ObjectMapper objectMapper;

    public UserResponse toResponse(User user) {
        var address = user.getAddress();
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .birthDate(user.getBirthDate())
                .role(user.getRole())
                .city(address != null ? address.getCity() : null)
                .district(address != null ? address.getDistrict() : null)
                .zipCode(address != null ? address.getZipCode() : null)
                .fullAddress(address != null ? address.getFullAddress() : null)
                .bio(user.getBio())
                .profilePictureUrl(user.getProfilePictureUrl())
                .skills(user.getSkills())
                .zoomEmail(user.getZoomEmail())
                .createdAt(user.getCreatedAt())
                .locked(user.isLocked())
                .build();
        response.setSkillsList(parseStringList(user.getSkills()));
        return response;
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank() || !json.trim().startsWith("[")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("JSON parse hatasi (skills): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public ProfileUpdateRequest toUpdateRequest(UserResponse user) {
        return ProfileUpdateRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(PhoneUtils.isProvided(user.getPhoneNumber()) ? user.getPhoneNumber() : "+90 ")
                .birthDate(user.getBirthDate())
                .city(user.getCity())
                .district(user.getDistrict())
                .zipCode(user.getZipCode())
                .fullAddress(user.getFullAddress())
                .zoomEmail(user.getZoomEmail())
                .build();
    }
}
