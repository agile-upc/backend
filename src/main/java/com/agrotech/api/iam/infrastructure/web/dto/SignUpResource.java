package com.agrotech.api.iam.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record SignUpResource(
        @NotBlank
        @Email
        String username,
        @NotBlank
        String password,
        @NotBlank
        String role,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @NotBlank
        String city,
        @NotBlank
        String country,
        @NotNull
        LocalDate birthDate,
        String description,
        @Schema(type = "string", format = "binary")
        MultipartFile photo,
        String occupation,
        String spokenLanguages,
        Integer experience
) {
}
