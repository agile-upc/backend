package com.agrotech.api.profile.infrastructure.web.controller;

import com.agrotech.api.profile.infrastructure.web.dto.AdvisorResource;
import com.agrotech.api.profile.infrastructure.web.dto.AdvisorCatalogResource;
import com.agrotech.api.profile.application.mapper.ProfileMapper;
import com.agrotech.api.profile.application.usecase.ProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "api/v1/advisors", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Advisors", description = "Advisor Management Endpoints")
public class AdvisorsController {
    private final ProfileService profileService;
    private final ProfileMapper profileMapper;

    public AdvisorsController(ProfileService profileService, ProfileMapper profileMapper) {
        this.profileService = profileService;
        this.profileMapper = profileMapper;
    }

    @PreAuthorize("hasRole('FARMER')")
    @GetMapping("/catalog")
    public ResponseEntity<List<AdvisorCatalogResource>> getAdvisorCatalog() {
        return ResponseEntity.ok(profileService.getAdvisorCatalog());
    }

    @GetMapping
    public ResponseEntity<AdvisorResource> getCurrentAdvisor() {
        return ResponseEntity.ok(profileMapper.toAdvisorResource(profileService.getCurrentAdvisor()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvisorResource> getAdvisorById(@PathVariable Long id) {
        return ResponseEntity.ok(profileMapper.toAdvisorResource(profileService.getAdvisorById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAdvisor(@PathVariable Long id) {
        profileService.deleteAdvisor(id);
        return ResponseEntity.ok("Advisor with id " + id + " deleted successfully");
    }
}
