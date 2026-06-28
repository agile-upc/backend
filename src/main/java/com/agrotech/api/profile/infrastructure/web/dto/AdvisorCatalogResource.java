package com.agrotech.api.profile.infrastructure.web.dto;

import java.math.BigDecimal;

public record AdvisorCatalogResource(
        Long advisorId,
        Long userId,
        BigDecimal rating,
        AdvisorProfileSummaryResource profile,
        boolean validated
) {
}
