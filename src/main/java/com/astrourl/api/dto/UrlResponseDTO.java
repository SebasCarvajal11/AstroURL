package com.astrourl.api.dto;
import java.time.LocalDateTime;

public record UrlResponseDTO(Long id, String shortCode, String longUrl, LocalDateTime createdAt, long clickCount) {}