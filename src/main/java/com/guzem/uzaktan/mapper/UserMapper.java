package com.guzem.uzaktan.mapper;

import com.guzem.uzaktan.dto.response.UserResponse;
import com.guzem.uzaktan.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        var address = user.getAddress();
        return UserResponse.builder()
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
                .createdAt(user.getCreatedAt())
                .locked(user.isLocked())
                .build();
    }
}
