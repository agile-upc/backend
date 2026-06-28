package com.agrotech.api.profile.infrastructure.web.dto;

import com.agrotech.api.profile.domain.valueobject.AdvisorCredentialStatus;

import java.util.Date;

public record AdvisorCredentialResource(
        Long id,
        Long advisorId,
        Long advisorUserId,
        String advisorName,
        String advisorPhoto,
        String certificateName,
        String issuingInstitution,
        String evidenceUrl,
        AdvisorCredentialStatus status,
        String reviewNotes,
        Long reviewedByUserId,
        Date createdAt,
        Date updatedAt
) {
}
