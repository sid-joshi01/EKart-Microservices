package com.ekart.user_service.mapper;

import com.ekart.user_service.dto.LoginResponse;
import com.ekart.user_service.dto.LonginRequest;
import com.ekart.user_service.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public LoginResponse toLoginResponse(User user, String token) {
        return new LoginResponse(token, "Bearer", user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
    }
}
