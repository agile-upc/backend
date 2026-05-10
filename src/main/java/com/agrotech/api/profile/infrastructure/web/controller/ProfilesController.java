package com.agrotech.api.profile.infrastructure.web.controller;

import com.agrotech.api.profile.infrastructure.web.dto.CreateProfileResource;
import com.agrotech.api.profile.infrastructure.web.dto.ProfileResource;
import com.agrotech.api.profile.infrastructure.web.dto.UpdateProfileResource;
import com.agrotech.api.profile.application.mapper.ProfileMapper;
import com.agrotech.api.profile.application.usecase.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "api/v1/profiles", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Profiles", description = "Profile Management Endpoints")
public class ProfilesController {
    private final ProfileService profileService;
    private final ProfileMapper profileMapper;

    public ProfilesController(ProfileService profileService, ProfileMapper profileMapper) {
        this.profileService = profileService;
        this.profileMapper = profileMapper;
    }

    @GetMapping
    public ResponseEntity<ProfileResource> getCurrentProfile() {
        return ResponseEntity.ok(profileMapper.toProfileResource(profileService.getCurrentProfile()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResource> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(profileMapper.toProfileResource(profileService.getProfileById(id)));
    }

    @Operation(summary = "Create profile", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = CreateProfileResource.class))))
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProfileResource> createProfile(@ModelAttribute CreateProfileResource createProfileResource) throws IOException {
        return new ResponseEntity<>(profileMapper.toProfileResource(profileService.createProfile(createProfileResource)), HttpStatus.CREATED);
    }

    @Operation(summary = "Update profile", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = UpdateProfileResource.class))))
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ProfileResource> updateProfile(
            @PathVariable Long id,
            @ModelAttribute UpdateProfileResource updateProfileResource
    ) throws IOException {
        return ResponseEntity.ok(profileMapper.toProfileResource(profileService.updateProfile(id, updateProfileResource)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProfile(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return ResponseEntity.ok("Profile with id " + id + " deleted successfully");
    }
}
