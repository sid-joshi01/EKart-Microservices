package com.ekart.product_service.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(LocalDateTime timestamp, int status, String message, String path) {
}
