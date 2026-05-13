package com.agrotech.api.profile.infrastructure.web.dto;

public record FarmerProfileSummaryResource(
        Long profileId,
        String firstName,
        String lastName,
        String city,
        String country,
        String description,
        String photo,
        String occupation,
        Integer experience
) {
}
