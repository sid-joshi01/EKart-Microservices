package com.ekart.api_gateway.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable()) // Keep CSRF disabled for APIs
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchanges -> exchanges

                                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Permit the registration path (include service name)
                        .pathMatchers("/api/users/auth/**").permitAll()

                        // Permit the JWKS endpoint so the Gateway can verify tokens
                        .pathMatchers("/.well-known/jwks.json").permitAll()
                        .pathMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
//                        .pathMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**").permitAll()

                        // Only admins can allow to call following methods
                        .pathMatchers(HttpMethod.POST, "/products/admin/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/products/admin/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/products/admin/**").hasRole("ADMIN")

                        // All other requests need a valid JWT
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(
                secret.getBytes(),
                "HmacSHA256"
        );

        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}