package com.agrotech.api.iam.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenResource(
        @NotBlank
        String refreshToken
) {
}
