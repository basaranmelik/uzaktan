package com.guzem.uzaktan.dto.response;

import com.guzem.uzaktan.model.common.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private LocalDate birthDate;
    private Role role;
    private String city;
    private String district;
    private String zipCode;
    private String fullAddress;
    private String bio;
    private String profilePictureUrl;
    private String skills;
    @Setter
    private List<String> skillsList;
    private String zoomEmail;
    private LocalDateTime createdAt;
    private boolean locked;
}
