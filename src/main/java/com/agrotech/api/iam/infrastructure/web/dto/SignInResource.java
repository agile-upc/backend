package com.agrotech.api.iam.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SignInResource(
        @NotBlank
        String username,
        @NotBlank
        String password
) {
}
