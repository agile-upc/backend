package com.agrotech.api.profile.application.mapper;

import com.agrotech.api.profile.domain.model.AdvisorCredential;
import com.agrotech.api.profile.domain.model.Profile;
import com.agrotech.api.profile.infrastructure.web.dto.AdvisorCredentialResource;
import org.springframework.stereotype.Component;

@Component
public class AdvisorCredentialMapper {
    public AdvisorCredentialResource toResource(AdvisorCredential credential, Profile profile) {
        String advisorName = profile == null
                ? "Advisor #" + credential.getAdvisor().getId()
                : (profile.getFirstName() + " " + profile.getLastName()).trim();

        return new AdvisorCredentialResource(
                credential.getId(),
                credential.getAdvisor().getId(),
                credential.getAdvisor().getUser().getId(),
                advisorName,
                profile == null ? null : profile.getPhoto(),
                credential.getCertificateName(),
                credential.getIssuingInstitution(),
                credential.getEvidenceUrl(),
                credential.getStatus(),
                credential.getReviewNotes(),
                credential.getReviewedByUserId(),
                credential.getCreatedAt(),
                credential.getUpdatedAt()
        );
    }
}
