package com.astrourl.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ShortenUrlRequestDTO(
        @NotBlank(message = "La URL es obligatoria")
        String url
) {}
