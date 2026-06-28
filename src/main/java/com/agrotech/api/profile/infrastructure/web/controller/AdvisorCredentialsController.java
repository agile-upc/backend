package com.agrotech.api.profile.infrastructure.web.controller;

import com.agrotech.api.profile.application.mapper.AdvisorCredentialMapper;
import com.agrotech.api.profile.application.usecase.AdvisorCredentialService;
import com.agrotech.api.profile.domain.model.AdvisorCredential;
import com.agrotech.api.profile.domain.model.Profile;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repository.ProfileRepository;
import com.agrotech.api.profile.infrastructure.web.dto.AdvisorCredentialResource;
import com.agrotech.api.profile.infrastructure.web.dto.CreateAdvisorCredentialResource;
import com.agrotech.api.profile.infrastructure.web.dto.ReviewAdvisorCredentialResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "api/v1/advisor-credentials", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Advisor Credentials", description = "Advisor Credential Validation Endpoints")
public class AdvisorCredentialsController {
    private final AdvisorCredentialService advisorCredentialService;
    private final AdvisorCredentialMapper advisorCredentialMapper;
    private final ProfileRepository profileRepository;

    public AdvisorCredentialsController(
            AdvisorCredentialService advisorCredentialService,
            AdvisorCredentialMapper advisorCredentialMapper,
            ProfileRepository profileRepository
    ) {
        this.advisorCredentialService = advisorCredentialService;
        this.advisorCredentialMapper = advisorCredentialMapper;
        this.profileRepository = profileRepository;
    }

    @GetMapping
    public ResponseEntity<List<AdvisorCredentialResource>> getCredentials() {
        return ResponseEntity.ok(toResources(advisorCredentialService.getCredentials()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<AdvisorCredentialResource>> getCurrentAdvisorCredentials() {
        return ResponseEntity.ok(toResources(advisorCredentialService.getCurrentAdvisorCredentials()));
    }

    @Operation(summary = "Submit advisor credential", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = CreateAdvisorCredentialResource.class))))
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<AdvisorCredentialResource> submitCredential(
            @ModelAttribute CreateAdvisorCredentialResource resource
    ) throws IOException {
        AdvisorCredential credential = advisorCredentialService.submitCredential(resource);
        Profile profile = profileRepository.findByUser_Id(credential.getAdvisor().getUser().getId()).orElse(null);
        return new ResponseEntity<>(advisorCredentialMapper.toResource(credential, profile), HttpStatus.CREATED);
    }

    @PatchMapping(value = "/{id}/review", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<AdvisorCredentialResource> reviewCredential(
            @PathVariable Long id,
            @RequestBody ReviewAdvisorCredentialResource resource
    ) {
        AdvisorCredential credential = advisorCredentialService.reviewCredential(id, resource);
        Profile profile = profileRepository.findByUser_Id(credential.getAdvisor().getUser().getId()).orElse(null);
        return ResponseEntity.ok(advisorCredentialMapper.toResource(credential, profile));
    }

    private List<AdvisorCredentialResource> toResources(List<AdvisorCredential> credentials) {
        Map<Long, Profile> profilesByUserId = profileRepository.findByUser_IdIn(credentials.stream()
                        .map(credential -> credential.getAdvisor().getUser().getId())
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), Function.identity()));

        return credentials.stream()
                .map(credential -> advisorCredentialMapper.toResource(
                        credential,
                        profilesByUserId.get(credential.getAdvisor().getUser().getId())
                ))
                .toList();
    }
}
