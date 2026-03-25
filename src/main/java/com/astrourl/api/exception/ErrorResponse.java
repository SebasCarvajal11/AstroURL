package com.astrourl.api.exception;
import java.time.LocalDateTime;

public record ErrorResponse(String errorCode, String message, LocalDateTime timestamp) {}