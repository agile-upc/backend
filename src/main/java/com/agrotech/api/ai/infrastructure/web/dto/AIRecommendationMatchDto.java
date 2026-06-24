package com.agrotech.api.ai.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AIRecommendationMatchDto(
        Long advisorId,
        String fullName,
        String occupation,
        BigDecimal rating,
        Integer experience,
        String city,
        String country,
        String spokenLanguages,
        LocalDate nextAvailableDate,
        String why
) {
}
