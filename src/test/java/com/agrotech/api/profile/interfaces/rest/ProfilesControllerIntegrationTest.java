package com.agrotech.api.profile.interfaces.rest;

import com.agrotech.api.AgrotechApplication;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.commands.SignInCommand;
import com.agrotech.api.iam.domain.model.commands.SignUpCommand;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.services.UserCommandService;
import com.agrotech.api.profile.domain.model.commands.CreateProfileCommand;
import com.agrotech.api.profile.domain.services.ProfileCommandService;
import com.agrotech.api.profile.interfaces.rest.resources.CreateProfileResource;
import com.agrotech.api.profile.interfaces.rest.resources.UpdateProfileResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(classes = AgrotechApplication.class)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ProfilesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserCommandService userCommandService;

    @Autowired
    private ProfileCommandService profileCommandService;


    private String token;
    private Long userId;

    @BeforeEach
    void setup() throws Throwable {
        User user = userCommandService.handle(new SignUpCommand("testuser@example.com", "password", List.of(Role.getDefaultRole())))
                .orElseThrow(() -> fail("User creation failed"));
        ImmutablePair<User, String> signInResult = userCommandService.handle(new SignInCommand("testuser@example.com", "password"))
                .orElseThrow(() -> fail("User sign-in failed"));
        this.token = signInResult.getRight();
        this.userId = user.getId();
    }

    @Test
    void getAllProfiles() throws Exception {
        mockMvc.perform(get("/api/v1/profiles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getProfileById() throws Exception {
        Long profileId = profileCommandService.handle(new CreateProfileCommand(userId, "John", "Doe", "Lima", "Peru", LocalDate.of(1990, 1, 1), "Description", "photo.jpg", null, null));

        mockMvc.perform(get("/api/v1/profiles/" + profileId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profileId));
    }

    @Test
    void getProfileByUserId() throws Exception {
        profileCommandService.handle(new CreateProfileCommand(userId, "John", "Doe", "Lima", "Peru", LocalDate.of(1990, 1, 1), "Description", "photo.jpg", null, null));

        mockMvc.perform(get("/api/v1/profiles/" + userId + "/user")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void createProfile() throws Exception {
        CreateProfileResource resource = new CreateProfileResource(userId, "John", "Doe", "Lima", "Peru", LocalDate.of(1990, 1, 1), "Description", "photo.jpg", null, null);

        mockMvc.perform(post("/api/v1/profiles")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void updateProfile() throws Exception {
        Long profileId = profileCommandService.handle(new CreateProfileCommand(userId, "John", "Doe", "Lima", "Peru", LocalDate.of(1990, 1, 1), "Description", "photo.jpg", null, null));

        UpdateProfileResource updateResource = new UpdateProfileResource("Jane", "Smith", "Cusco", "Peru", LocalDate.of(1995, 5, 5), "Updated description", "updated_photo.jpg", "Engineer", 5);

        mockMvc.perform(put("/api/v1/profiles/" + profileId)
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(updateResource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    void deleteProfile() throws Exception {
        Long profileId = profileCommandService.handle(new CreateProfileCommand(userId, "John", "Doe", "Lima", "Peru", LocalDate.of(1990, 1, 1), "Description", "photo.jpg", null, null));

        mockMvc.perform(delete("/api/v1/profiles/" + profileId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Profile with id " + profileId + " deleted successfully"));
    }
}