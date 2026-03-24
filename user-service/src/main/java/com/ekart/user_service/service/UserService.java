package com.ekart.user_service.service;


import com.ekart.user_service.dto.*;
import com.ekart.user_service.entity.User;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface UserService {
    UserResponse register(UserCreateRequest userCreateRequest);

    AuthResponse login(LonginRequest longinRequest);

    UserResponse findById(Long id);

    AuthResponse loginWithGoogle(String email, String firstName, String lastName);

    User registerWithGoogle(String email, String firstName, String lastName);
}
