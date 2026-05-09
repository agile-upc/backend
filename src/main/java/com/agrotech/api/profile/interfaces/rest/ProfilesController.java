package com.agrotech.api.profile.interfaces.rest;

import com.agrotech.api.appointment.domain.exceptions.AppointmentNotFoundException;
import com.agrotech.api.appointment.domain.exceptions.ProfileNotFoundException;
import com.agrotech.api.profile.domain.model.aggregates.Profile;
import com.agrotech.api.profile.domain.model.commands.DeleteProfileCommand;
import com.agrotech.api.profile.domain.model.queries.GetAllAdvisorProfilesQuery;
import com.agrotech.api.profile.domain.model.queries.GetAllProfilesQuery;
import com.agrotech.api.profile.domain.model.queries.GetProfileByIdQuery;
import com.agrotech.api.profile.domain.model.queries.GetProfileByUserIdQuery;
import com.agrotech.api.profile.domain.services.ProfileCommandService;
import com.agrotech.api.profile.domain.services.ProfileQueryService;
import com.agrotech.api.profile.interfaces.rest.resources.CreateProfileResource;
import com.agrotech.api.profile.interfaces.rest.resources.ProfileResource;
import com.agrotech.api.profile.interfaces.rest.resources.UpdateProfileResource;
import com.agrotech.api.profile.interfaces.rest.transform.CreateProfileCommandFromResourceAssembler;
import com.agrotech.api.profile.interfaces.rest.transform.ProfileResourceFromEntityAssembler;
import com.agrotech.api.profile.interfaces.rest.transform.UpdateProfileCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RestController
@RequestMapping(value="api/v1/profiles", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Profiles", description = "Profile Management Endpoints")
public class ProfilesController {
    private final ProfileCommandService profileCommandService;
    private final ProfileQueryService profileQueryService;

    public ProfilesController(ProfileCommandService profileCommandService, ProfileQueryService profileQueryService) {
        this.profileCommandService = profileCommandService;
        this.profileQueryService = profileQueryService;
    }

    @GetMapping
    public ResponseEntity<List<ProfileResource>> getAllProfiles() {
        var getAllProfilesQuery = new GetAllProfilesQuery();
        var profiles = profileQueryService.handle(getAllProfilesQuery);
        var profileResources = profiles.stream().map(ProfileResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(profileResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResource> getProfileById(@PathVariable Long id) {
        var getProfileByIdQuery = new GetProfileByIdQuery(id);
        var profile = profileQueryService.handle(getProfileByIdQuery);
        if (profile.isEmpty()) return ResponseEntity.notFound().build();
        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile.get());
        return ResponseEntity.ok(profileResource);
    }

    @GetMapping("/{userId}/user")
    public ResponseEntity<ProfileResource> getProfileByUserId(@PathVariable Long userId) {
        var getProfileByUserIdQuery = new GetProfileByUserIdQuery(userId);
        var profile = profileQueryService.handle(getProfileByUserIdQuery);
        if (profile.isEmpty()) throw new ProfileNotFoundException();
        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile.get());
        return ResponseEntity.ok(profileResource);
    }

    @GetMapping("/advisors")
    public ResponseEntity<List<ProfileResource>> getAdvisors() {
        var GetAllAdvisorProfilesQuery = new GetAllAdvisorProfilesQuery();
        var profiles = profileQueryService.handle(GetAllAdvisorProfilesQuery);
        var profileResources = profiles.stream().map(ProfileResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(profileResources);
    }

    @Operation(summary = "Create profile", requestBody = @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = CreateProfileResource.class))))
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProfileResource> createProfile(
            @ModelAttribute CreateProfileResource createProfileResource) throws IOException {
        var createProfileCommand = CreateProfileCommandFromResourceAssembler.toCommandFromResource(createProfileResource);
        Long profileId = profileCommandService.handle(createProfileCommand);
        var profile = profileQueryService.handle(new GetProfileByIdQuery(profileId));
        if (profile.isEmpty()) return ResponseEntity.badRequest().build();
        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile.get());
        return new ResponseEntity<>(profileResource, HttpStatus.CREATED);
    }

    @Operation(summary = "Update profile", requestBody = @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = UpdateProfileResource.class))))
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ProfileResource> updateProfile(
            @PathVariable Long id,
            @ModelAttribute UpdateProfileResource updateProfileResource) throws IOException {
        var updateProfileCommand = UpdateProfileCommandFromResourceAssembler.toCommandFromResource(id, updateProfileResource);
        Optional<Profile> profile = profileCommandService.handle(updateProfileCommand);
        if (profile.isEmpty()) return ResponseEntity.notFound().build();
        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile.get());
        return ResponseEntity.ok(profileResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfile(@PathVariable Long id) {
        var deleteProfileCommand = new DeleteProfileCommand(id);
        profileCommandService.handle(deleteProfileCommand);
        return ResponseEntity.ok("Profile with id " + id + " deleted successfully");
    }

}
