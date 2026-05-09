package com.agrotech.api.profile.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record CreateProfileResource(
        @NotNull
        Long userId,
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
