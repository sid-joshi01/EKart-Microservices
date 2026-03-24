package com.ekart.user_service.config;

import com.ekart.user_service.dto.AuthResponse;
import com.ekart.user_service.exception.UnauthorizedUserException;
import com.ekart.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.awt.*;
import java.io.IOException;
import java.util.Map;

@Component
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;


    public Oauth2SuccessHandler(@Lazy UserService userService) {
        this.userService = userService;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        if(authToken != null) {
//            OAuth2User oAuth2User = authToken.getPrincipal();
//            String registrationId = authToken.getAuthorizedClientRegistrationId();

            Map<String, Object> attributes = authToken.getPrincipal().getAttributes();
            String email = (String) attributes.get("email");        // Google email
            String firstName = (String) attributes.get("given_name");
            String lastName = (String) attributes.get("family_name");


            AuthResponse loginResponse = userService.loginWithGoogle(email, firstName, lastName);
            String jwtToken = loginResponse.token();

            // 2. Redirect back to Angular (e.g., localhost:4200) with the token in the URL
            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:4200/login")
                    .queryParam("token", jwtToken)
                    .build().toUriString();

            // 3. Use the built-in redirect strategy
            response.sendRedirect(targetUrl);


//            response.setContentType(MediaType.APPLICATION_JSON);
//            response.setStatus(HttpServletResponse.SC_OK);
//            response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
        }else {
            // Redirect to login with an error param
            response.sendRedirect("http://localhost:4200/login?error=auth_failed");
            throw new UnauthorizedUserException("Google Authentication failed");
        }

    }


}
