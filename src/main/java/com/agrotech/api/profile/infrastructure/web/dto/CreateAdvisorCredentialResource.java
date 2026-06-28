package com.agrotech.api.profile.infrastructure.web.dto;

import org.springframework.web.multipart.MultipartFile;

public record CreateAdvisorCredentialResource(
        String certificateName,
        String issuingInstitution,
        String evidenceUrl,
        MultipartFile evidenceFile
) {
}
