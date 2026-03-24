package com.ekart.user_service.mapper;


import com.ekart.user_service.dto.UserCreateRequest;
import com.ekart.user_service.dto.UserResponse;
import com.ekart.user_service.entity.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserCreateRequest userCreateRequest, String encodedPassword) {
        return User.builder().firstName(userCreateRequest.getFirstName())
                .lastName(userCreateRequest.getLastName())
                .email(userCreateRequest.getEmail())
                .password(encodedPassword)
                .phone(userCreateRequest.getPhone())
                .role("USER")
                .build();
    }

    public UserResponse toDto(User user) {
        return UserResponse.builder().id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
