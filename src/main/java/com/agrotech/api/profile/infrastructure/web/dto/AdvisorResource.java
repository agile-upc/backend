package com.agrotech.api.profile.infrastructure.web.dto;

import java.math.BigDecimal;

public record AdvisorResource(Long id,
                              Long userId,
                              BigDecimal rating,
                              boolean validated) {
}
