package com.agrotech.api.profile.infrastructure.web.controller;

import com.agrotech.api.profile.infrastructure.web.dto.FarmerResource;
import com.agrotech.api.profile.application.mapper.ProfileMapper;
import com.agrotech.api.profile.application.usecase.ProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "api/v1/farmers", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Farmers", description = "Farmer Management Endpoints")
public class FarmersController {
    private final ProfileService profileService;
    private final ProfileMapper profileMapper;

    public FarmersController(ProfileService profileService, ProfileMapper profileMapper) {
        this.profileService = profileService;
        this.profileMapper = profileMapper;
    }

    @GetMapping
    public ResponseEntity<FarmerResource> getCurrentFarmer() {
        return ResponseEntity.ok(profileMapper.toFarmerResource(profileService.getCurrentFarmer()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FarmerResource> getFarmerById(@PathVariable Long id) {
        return ResponseEntity.ok(profileMapper.toFarmerResource(profileService.getFarmerById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFarmer(@PathVariable Long id) {
        profileService.deleteFarmer(id);
        return ResponseEntity.ok("Farmer with id " + id + " deleted successfully");
    }
}
