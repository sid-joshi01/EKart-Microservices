package com.ekart.user_service.serviceImpl;

import com.ekart.user_service.dto.*;
import com.ekart.user_service.entity.User;
import com.ekart.user_service.exception.ResourceAlreadyExistException;
import com.ekart.user_service.exception.UnauthorizedUserException;
import com.ekart.user_service.exception.UserNotFoundException;
import com.ekart.user_service.mapper.AuthMapper;
import com.ekart.user_service.mapper.UserMapper;
import com.ekart.user_service.repository.UserRepository;
import com.ekart.user_service.service.UserService;
import com.ekart.user_service.util.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.transaction.Transactional;
import jdk.jshell.spi.ExecutionControl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

// Google Auth Library Imports
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserMapper userMapper;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final AuthMapper authMapper;


    @Override
    public UserResponse register(UserCreateRequest userCreateRequest) {
        if(userRepository.existsByEmail(userCreateRequest.getEmail())) {
            throw new ResourceAlreadyExistException("Email already exists");
        }
        String encodedPassword = passwordEncoder.encode(userCreateRequest.getPassword());
        User user = userMapper.toEntity(userCreateRequest, encodedPassword);
        user.setProvider("LOCAL");
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    public AuthResponse login(LonginRequest longinRequest) {
        User user = userRepository.findByEmail(longinRequest.getEmail()).orElseThrow(() -> new UserNotFoundException("User not found with email : " + longinRequest.getEmail()));
        boolean matches = passwordEncoder.matches(longinRequest.getPassword(), user.getPassword());
        if(!matches) {
            throw new UserNotFoundException("Invalid password");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(),  user.getRole());
        return new AuthResponse(token, user.getId());
    }

    @Override
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id : " + id));
        return userMapper.toDto(user);
    }


    // google will create login page if you don't provide yours at following endpoint
    //  http://localhost:8080/oauth2/authorization/google
    @Override
    @Transactional
    public AuthResponse loginWithGoogle(String email, String firstName, String lastName) {

        if (email != null) {
            // 2. Fetch/Register User in your DB
            User user = userRepository.findByEmail(email).orElseGet(() -> registerWithGoogle(email,firstName,lastName));

            // 3. Create YOUR JWT
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(),  user.getRole());
//            return authMapper.toLoginResponse(user, token);
            return new AuthResponse(token, user.getId());

        }else {
            throw new UnauthorizedUserException("Google Authentication failed");
        }
    }


//    @Override
//    public LoginResponse loginWithGoogle(String email, String firstName, String lastName) throws GeneralSecurityException, IOException {
//        // 1. Verify Google Token using Google's library
////        GoogleIdToken idToken = verifier.verify(idTokenString);
//        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
//                .setAudience(Collections.singletonList(googleClientId)) // Go to the Google Cloud Console.
//                .build();
//
//        GoogleIdToken idToken = verifier.verify(idTokenString);
//        if (idToken != null) {
//            Payload payload = idToken.getPayload();
//            String email = payload.getEmail();
//            String firstName = (String) payload.get("given_name");
//            String lastName = (String) payload.get("family_name");
//
//
//            // 2. Fetch/Register User in your DB
//            User user = userRepository.findByEmail(email).orElseGet(() -> registerWithGoogle(email,firstName,lastName));
//
//            // 3. Create YOUR JWT
//            String token = jwtUtil.generateToken(user.getId(), user.getEmail(),  user.getRole());
//            return authMapper.toLoginResponse(user, token);
//        }else {
//            throw new UnauthorizedUserException("Google Authentication failed");
//        }
//    }

    @Override
    public User registerWithGoogle(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole("USER");
        user.setProvider("GOOGLE");
        return userRepository.save(user);
    }
}
