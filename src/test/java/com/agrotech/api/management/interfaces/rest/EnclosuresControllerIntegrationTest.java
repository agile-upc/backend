package com.agrotech.api.management.interfaces.rest;

import com.agrotech.api.AgrotechApplication;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.commands.SignInCommand;
import com.agrotech.api.iam.domain.model.commands.SignUpCommand;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.services.UserCommandService;
import com.agrotech.api.management.domain.model.aggregates.Enclosure;
import com.agrotech.api.management.domain.model.commands.CreateEnclosureCommand;
import com.agrotech.api.management.domain.model.queries.GetEnclosureByIdQuery;
import com.agrotech.api.management.domain.services.EnclosureCommandService;
import com.agrotech.api.management.domain.services.EnclosureQueryService;
import com.agrotech.api.management.interfaces.rest.resources.CreateEnclosureResource;
import com.agrotech.api.management.interfaces.rest.resources.UpdateEnclosureResource;
import com.agrotech.api.profile.domain.model.commands.CreateFarmerCommand;
import com.agrotech.api.profile.domain.model.entities.Farmer;
import com.agrotech.api.profile.domain.model.queries.GetFarmerByIdQuery;
import com.agrotech.api.profile.domain.services.FarmerCommandService;
import com.agrotech.api.profile.domain.services.FarmerQueryService;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(classes = AgrotechApplication.class)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class EnclosuresControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FarmerCommandService farmerCommandService;
    @Autowired
    private FarmerQueryService farmerQueryService;
    @Autowired
    private EnclosureCommandService enclosureCommandService;
    @Autowired
    private EnclosureQueryService enclosureQueryService;
    @Autowired
    private UserCommandService userCommandService;

    private String token;
    private Farmer farmer;

    @BeforeEach
    void setup() throws Throwable { // fail es un Throwable
        User user = userCommandService.handle(new SignUpCommand("testuser@example.com", "password", List.of(Role.getDefaultRole())))
                .orElseThrow(() -> fail("User creation failed"));
        ImmutablePair<User, String> signInResult = userCommandService.handle(new SignInCommand("testuser@example.com", "password"))
                .orElseThrow(() -> fail("User sign-in failed"));
        Long farmerId = farmerCommandService.handle(new CreateFarmerCommand(user.getId()), user);
        Farmer farmer = farmerQueryService.handle(new GetFarmerByIdQuery(farmerId))
                .orElseThrow(() -> fail("Farmer creation failed"));
        this.token = signInResult.getRight();
        this.farmer = farmer;
    }

    @Test
    void postEnclosure() throws Exception {
        // Arrange
        CreateEnclosureResource resource = new CreateEnclosureResource("Barn", 10, "Barn Type", farmer.getId());
        // Act
        mockMvc.perform(post("/api/v1/enclosures")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Barn"))
                .andExpect(jsonPath("$.capacity").value(10))
                .andExpect(jsonPath("$.type").value("Barn Type"));
    }

    @Test
    void getEnclosures() throws Exception {
        // Arrange
        CreateEnclosureCommand commandOne = new CreateEnclosureCommand("Stable", 6, "Horse", farmer.getId());
        CreateEnclosureCommand commandTwo = new CreateEnclosureCommand("Corral", 4, "Cow", farmer.getId());
        // Act
        enclosureCommandService.handle(commandOne);
        enclosureCommandService.handle(commandTwo);
        mockMvc.perform(get("/api/v1/enclosures")
                        .header("Authorization", "Bearer " + token))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Stable"))
                .andExpect(jsonPath("$[1].name").value("Corral"));
    }

    @Test
    void updateEnclosure() throws Throwable {
        // Arrange
        CreateEnclosureCommand command = new CreateEnclosureCommand("Old Corral", 3, "Pig", farmer.getId());
        Long enclosureId = enclosureCommandService.handle(command);
        Enclosure enclosure = enclosureQueryService.handle(new GetEnclosureByIdQuery(enclosureId))
                .orElseThrow(() -> fail("Enclosure creation failed"));
        UpdateEnclosureResource updateResource = new UpdateEnclosureResource("New Corral", 5, "Pig");
        // Act
        mockMvc.perform(put("/api/v1/enclosures/" + enclosure.getId())
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(updateResource)))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Corral"))
                .andExpect(jsonPath("$.capacity").value(5));
    }

    @Test
    void deleteEnclosure() throws Exception {
        // Arrange
        CreateEnclosureCommand command = new CreateEnclosureCommand("Temporary", 2, "Goat", farmer.getId());
        Long enclosureId = enclosureCommandService.handle(command);
        // Act
        mockMvc.perform(delete("/api/v1/enclosures/" + enclosureId)
                        .header("Authorization", "Bearer " + token))
                // Assert
                .andExpect(status().isOk());
        assertFalse(enclosureQueryService.handle(new GetEnclosureByIdQuery(enclosureId)).isPresent(), "Enclosure should be deleted");
    }

    @Test
    void postEnclosureWithInvalidToken() throws Exception {
        CreateEnclosureResource resource = new CreateEnclosureResource("Invalid", 3, "Rare Type", 1L);

        mockMvc.perform(post("/api/v1/enclosures")
                        .contentType("application/json")
                        .header("Authorization", "Bearer invalid_token")
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getEnclosuresWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/enclosures")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateEnclosureWithInvalidToken() throws Exception {
        UpdateEnclosureResource update = new UpdateEnclosureResource("Invalid", 1, "Sheep");

        mockMvc.perform(put("/api/v1/enclosures/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer invalid_token")
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteEnclosureWithInvalidToken() throws Exception {
        mockMvc.perform(delete("/api/v1/enclosures/1")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }
}
