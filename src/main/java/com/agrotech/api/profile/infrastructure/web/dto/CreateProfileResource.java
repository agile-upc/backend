package com.agrotech.api.profile.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record CreateProfileResource(
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
        @NotNull
        MultipartFile photo,
        String occupation,
        Integer experience
){}
