package com.agrotech.api.profile.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record UpdateProfileResource(
        @NotNull
        String firstName,
        @NotNull
        String lastName,
        @NotNull
        String city,
        @NotNull
        String country,
        @NotNull
        LocalDate birthDate,
        String description,
        @Schema(type = "string", format = "binary")
        MultipartFile photo,
        String occupation,
        Integer experience
){}
