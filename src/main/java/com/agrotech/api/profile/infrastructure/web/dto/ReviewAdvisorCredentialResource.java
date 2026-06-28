package com.agrotech.api.profile.infrastructure.web.dto;

import com.agrotech.api.profile.domain.valueobject.AdvisorCredentialStatus;

public record ReviewAdvisorCredentialResource(
        AdvisorCredentialStatus status,
        String reviewNotes
) {
}
