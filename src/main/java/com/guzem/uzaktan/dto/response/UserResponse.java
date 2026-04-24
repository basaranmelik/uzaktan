package com.guzem.uzaktan.dto.response;

import com.guzem.uzaktan.model.common.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
    private boolean locked;
}
