package com.agrotech.api.profile.infrastructure.web.controller;

import com.agrotech.api.profile.application.mapper.ProfileMapper;
import com.agrotech.api.profile.application.usecase.AdvisorService;
import com.agrotech.api.profile.infrastructure.web.dto.AdvisorCatalogResource;
import com.agrotech.api.profile.infrastructure.web.dto.AdvisorResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "api/v1/advisors", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Advisors", description = "Advisor Management Endpoints")
public class AdvisorsController {
    private final AdvisorService advisorService;
    private final ProfileMapper profileMapper;

    public AdvisorsController(AdvisorService advisorService, ProfileMapper profileMapper) {
        this.advisorService = advisorService;
        this.profileMapper = profileMapper;
    }

    @PreAuthorize("hasRole('FARMER')")
    @GetMapping("/catalog")
    public ResponseEntity<List<AdvisorCatalogResource>> getAdvisorCatalog() {
        return ResponseEntity.ok(advisorService.getAdvisorCatalog());
    }

    @GetMapping
    public ResponseEntity<AdvisorResource> getCurrentAdvisor() {
        return ResponseEntity.ok(profileMapper.toAdvisorResource(advisorService.getCurrentAdvisor()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvisorResource> getAdvisorById(@PathVariable Long id) {
        return ResponseEntity.ok(profileMapper.toAdvisorResource(advisorService.getAdvisorById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAdvisor(@PathVariable Long id) {
        advisorService.deleteAdvisor(id);
        return ResponseEntity.ok("Advisor with id " + id + " deleted successfully");
    }
}
