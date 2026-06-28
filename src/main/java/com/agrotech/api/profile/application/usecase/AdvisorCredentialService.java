package com.agrotech.api.profile.application.usecase;

import com.agrotech.api.iam.application.usecase.AuthenticatedUserService;
import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.domain.model.AdvisorCredential;
import com.agrotech.api.profile.domain.valueobject.AdvisorCredentialStatus;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repository.AdvisorCredentialRepository;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repository.AdvisorRepository;
import com.agrotech.api.profile.infrastructure.web.dto.CreateAdvisorCredentialResource;
import com.agrotech.api.profile.infrastructure.web.dto.ReviewAdvisorCredentialResource;
import com.agrotech.api.shared.infrastructure.storage.GoogleStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdvisorCredentialService {
    private final AdvisorCredentialRepository advisorCredentialRepository;
    private final AdvisorRepository advisorRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final GoogleStorageService googleStorageService;

    public AdvisorCredentialService(
            AdvisorCredentialRepository advisorCredentialRepository,
            AdvisorRepository advisorRepository,
            AuthenticatedUserService authenticatedUserService,
            GoogleStorageService googleStorageService
    ) {
        this.advisorCredentialRepository = advisorCredentialRepository;
        this.advisorRepository = advisorRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.googleStorageService = googleStorageService;
    }

    public List<AdvisorCredential> getCredentials() {
        AuthenticatedUser authenticatedUser = authenticatedUserService.getCurrentUser();
        if (authenticatedUser.role() == UserRole.ADMIN) {
            return advisorCredentialRepository.findAllByOrderByCreatedAtDesc();
        }
        if (authenticatedUser.role() == UserRole.ADVISOR) {
            return advisorCredentialRepository.findByAdvisor_IdOrderByCreatedAtDesc(authenticatedUser.advisorId());
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operation requires role ADMIN or ADVISOR");
    }

    public List<AdvisorCredential> getCurrentAdvisorCredentials() {
        Long advisorId = authenticatedUserService.requireRole(UserRole.ADVISOR).advisorId();
        return advisorCredentialRepository.findByAdvisor_IdOrderByCreatedAtDesc(advisorId);
    }

    @Transactional
    public AdvisorCredential submitCredential(CreateAdvisorCredentialResource resource) throws IOException {
        Long advisorId = authenticatedUserService.requireRole(UserRole.ADVISOR).advisorId();
        Advisor advisor = advisorRepository.findById(advisorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Advisor not found"));

        String evidenceUrl = resolveEvidenceUrl(resource);
        if (!hasText(resource.certificateName()) || !hasText(resource.issuingInstitution()) || !hasText(evidenceUrl)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "certificateName, issuingInstitution and evidence are required");
        }

        AdvisorCredential credential = AdvisorCredential.builder()
                .advisor(advisor)
                .certificateName(resource.certificateName().trim())
                .issuingInstitution(resource.issuingInstitution().trim())
                .evidenceUrl(evidenceUrl)
                .status(AdvisorCredentialStatus.PENDING)
                .build();

        return advisorCredentialRepository.save(credential);
    }

    @Transactional
    public AdvisorCredential reviewCredential(Long id, ReviewAdvisorCredentialResource resource) {
        AuthenticatedUser authenticatedUser = authenticatedUserService.requireRole(UserRole.ADMIN);
        if (resource == null || resource.status() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }

        AdvisorCredential credential = advisorCredentialRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Advisor credential not found"));

        credential.setStatus(resource.status());
        credential.setReviewNotes(trimToNull(resource.reviewNotes()));
        credential.setReviewedByUserId(authenticatedUser.userId());
        credential.setReviewedAt(LocalDateTime.now());

        return advisorCredentialRepository.save(credential);
    }

    private String resolveEvidenceUrl(CreateAdvisorCredentialResource resource) throws IOException {
        if (resource == null) {
            return null;
        }
        if (resource.evidenceFile() != null && !resource.evidenceFile().isEmpty()) {
            return googleStorageService.uploadFile(resource.evidenceFile());
        }
        return trimToNull(resource.evidenceUrl());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
