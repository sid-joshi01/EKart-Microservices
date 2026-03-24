package com.ekart.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String secretKey;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {
        private List<String> requiredRole;
        // Getters and Setters
        public List<String> getRequiredRole() { return requiredRole; }
        public void setRequiredRole(List<String> requiredRole) { this.requiredRole = requiredRole; }
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 1. Check for Authorization Header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.info("Authorization required");
                return onError(exchange, "No Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);
            if (!authHeader.startsWith("Bearer ")) {
                log.info("Authorization header not found with bearer");
                return onError(exchange, "Invalid Token Format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            log.info("JWT Token: {}", token);

            try {
                // 1. Prepare the SecretKey (Ensure your secretKey string is at least 32 characters)
                SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

                // 2. Validate Signature and Extract Claims using 0.12.x syntax
                Claims claims = Jwts.parser()
                        .verifyWith(key)               // Replaces setSigningKey()
                        .build()
                        .parseSignedClaims(token)      // Replaces parseClaimsJws()
                        .getPayload();                 // Replaces getBody()

                // 3. Authorization: Check Roles
                List<String> roles = claims.get("roles", List.class);
                List<String> allowedRoles = config.getRequiredRole();

                if (allowedRoles != null && (roles == null || roles.stream().noneMatch(allowedRoles::contains))) {

                    log.info("Roles not found");
                    return onError(exchange, "Insufficient Permissions", HttpStatus.FORBIDDEN);
                }

                // Ensure roles is not null before joining to avoid 401/catch block trigger
                String rolesString = (roles != null) ? String.join(",", roles) : "";

                // 4. Mutate Request: Forward User Info Downstream
                // This prevents the next service from needing to parse the JWT again
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", claims.getSubject())
                        .header("X-User-Roles", rolesString)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                // This will print the EXACT reason in your Gateway console (Signature mismatch, Expired, etc.)
                log.info("JWT Validation failed");
                e.printStackTrace();
                return onError(exchange, "Token Validation Failed", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }
}