package com.ekart.api_gateway.routes;


import com.ekart.api_gateway.filter.JwtAuthenticationFilter;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Arrays;


@Configuration
public class Routes {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, JwtAuthenticationFilter authFilter, RedisRateLimiter redisRateLimiter,
                                           KeyResolver ipKeyResolver) {
        return builder.routes()
                // PUBLIC/USER: Read-only access (GET)
                    .route("product_view", r -> r.path("/api/products/**")
                            .and().method("GET")
                            .filters(f -> f.filter(authFilter.apply(createConfig("USER", "ADMIN")))
                                    .requestRateLimiter(config -> {config.setRateLimiter(redisRateLimiter);
                                    config.setKeyResolver(ipKeyResolver);
                                    })
                            )
                            .uri("lb://PRODUCT-SERVICE"))

                // ADMIN ONLY: Write access (POST, PUT, DELETE)
                .route("product_admin", r -> r.path("/api/products/admin/**")
                        .and().method("GET", "POST", "PUT", "DELETE")
                        .filters(f -> f.filter(authFilter.apply(createConfig("ADMIN")))
                                .requestRateLimiter(config -> {config.setRateLimiter(redisRateLimiter);
                                    config.setKeyResolver(ipKeyResolver);
                                })
                        )
                        .uri("lb://PRODUCT-SERVICE"))


                // Order Service - Requires USER role
                .route("order_service", r -> r.path("/api/order/**")
                        .filters(f -> f.filter(authFilter.apply(createConfig("USER", "ADMIN")))
                                .requestRateLimiter(config -> {config.setRateLimiter(redisRateLimiter);
                                    config.setKeyResolver(ipKeyResolver);
                                })
                        )
                        .uri("lb://ORDER-SERVICE"))

                // Inventory Service - Requires ADMIN role
                .route("inventory_admin", r -> r.path("/api/inventory/admin/**")
                        .and().method("GET", "POST", "PUT", "DELETE")
                        .filters(f -> f.filter(authFilter.apply(createConfig("ADMIN")))
                                .requestRateLimiter(config -> {config.setRateLimiter(redisRateLimiter);
                                    config.setKeyResolver(ipKeyResolver);
                                })
                        )
                        .uri("lb://INVENTORY-SERVICE"))

                .route("inventory_service", r -> r.path("/api/inventory/**")
                        .filters(f -> f.filter(authFilter.apply(createConfig("USER", "ADMIN")))
                                .requestRateLimiter(config -> {config.setRateLimiter(redisRateLimiter);
                                    config.setKeyResolver(ipKeyResolver);
                                })
                        )
                        .uri("lb://INVENTORY-SERVICE"))


                // Cart Service
                .route("cart_service", r -> r.path("/api/cart/**")
                        .filters(f -> f.filter(authFilter.apply(createConfig("USER")))
                                .requestRateLimiter(config -> {config.setRateLimiter(redisRateLimiter);
                                    config.setKeyResolver(ipKeyResolver);
                                })
                        )
                        .uri("lb://CART-SERVICE"))

                
                // Payment Service
                .route("payment_service", r -> r.path("/api/payments/**")
                        .filters(f -> f.filter(authFilter.apply(createConfig("USER", "ADMIN")))
                                .requestRateLimiter(config -> {config.setRateLimiter(redisRateLimiter);
                                    config.setKeyResolver(ipKeyResolver);
                                })
                        )
                        .uri("lb://PAYMENT-SERVICE"))


                .route("google-oauth-callback", r -> r
                        .path("/login/oauth2/code/google", "/oauth2/authorization/google") // The standard Spring Security path
                        .filters(f -> f
                                .preserveHostHeader() // Keeps the public domain name intact
                                .removeRequestHeader("Expect"))
                        .uri("lb://USER-SERVICE")) // Routes to the User Service via Load Balancer

                // Public Auth Route (No Filter)
                .route("user_auth_service", r -> r.path("/api/users/auth/**")
                        .filters(f -> f.requestRateLimiter(config -> {
                            config.setRateLimiter(redisRateLimiter);
                            config.setKeyResolver(ipKeyResolver);
                        }))
                        .uri("lb://USER-SERVICE"))
                // 2. Protected User Route
                .route("user_service", r -> r.path("/api/users/**")
                        .filters(f -> f.filter(authFilter.apply(createConfig("USER")))
                                .requestRateLimiter(config -> {config.setRateLimiter(redisRateLimiter);
                                    config.setKeyResolver(ipKeyResolver);
                                })
                        )
                        .uri("lb://USER-SERVICE"))



                .build();
    }

    // Helper method to set the role in config
    private JwtAuthenticationFilter.Config createConfig(String... roles) {
        JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
        config.setRequiredRole(Arrays.asList(roles));
        return config;
    }




}
