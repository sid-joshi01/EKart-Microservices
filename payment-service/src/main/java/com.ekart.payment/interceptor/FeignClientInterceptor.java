package com.ekart.payment.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
//    private static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

    @Override
    public void apply(RequestTemplate requestTemplate) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("FeignClientInterceptor authentication: {}", authentication);

        if(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String tokenValue = jwtAuthenticationToken.getToken().getTokenValue();
            log.info("Forwarding JWT to order service: {}", tokenValue);
            requestTemplate.header(AUTHORIZATION_HEADER, "Bearer " + tokenValue);
        }

    }
}
