package com.agrotech.api.management.interfaces.rest;

import com.agrotech.api.AgrotechApplication;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.commands.SignInCommand;
import com.agrotech.api.iam.domain.model.commands.SignUpCommand;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.services.UserCommandService;
import com.agrotech.api.management.domain.model.aggregates.Enclosure;
import com.agrotech.api.management.domain.model.commands.CreateAnimalCommand;
import com.agrotech.api.management.domain.model.commands.CreateEnclosureCommand;
import com.agrotech.api.management.domain.model.entities.Animal;
import com.agrotech.api.management.domain.model.queries.GetAnimalByIdQuery;
import com.agrotech.api.management.domain.model.queries.GetEnclosureByIdQuery;
import com.agrotech.api.management.domain.model.valueobjects.HealthStatus;
import com.agrotech.api.management.domain.services.AnimalCommandService;
import com.agrotech.api.management.domain.services.AnimalQueryService;
import com.agrotech.api.management.domain.services.EnclosureCommandService;
import com.agrotech.api.management.domain.services.EnclosureQueryService;
import com.agrotech.api.management.interfaces.rest.resources.CreateAnimalResource;
import com.agrotech.api.management.interfaces.rest.resources.UpdateAnimalResource;
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
class AnimalsControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AnimalCommandService animalCommandService;
    @Autowired
    private AnimalQueryService animalQueryService;
    @Autowired
    private EnclosureCommandService enclosureCommandService;
    @Autowired
    private EnclosureQueryService enclosureQueryService;
    @Autowired
    private FarmerCommandService farmerCommandService;
    @Autowired
    private FarmerQueryService farmerQueryService;
    @Autowired
    private UserCommandService userCommandService;

    private String token;
    private Long enclosureId;

    @BeforeEach
    void setup() throws Throwable { // fail es un Throwable
        User user = userCommandService.handle(new SignUpCommand("testuser@example.com", "password", List.of(Role.getDefaultRole())))
                .orElseThrow(() -> fail("User creation failed"));
        ImmutablePair<User, String> signInResult = userCommandService.handle(new SignInCommand("testuser@example.com", "password"))
                .orElseThrow(() -> fail("User sign-in failed"));
        Long farmerId = farmerCommandService.handle(new CreateFarmerCommand(user.getId()), user);
        Farmer farmer = farmerQueryService.handle(new GetFarmerByIdQuery(farmerId))
                .orElseThrow(() -> fail("Farmer creation failed"));
        // Create an enclosure for animal tests
        Long enclosureId = enclosureCommandService.handle(new CreateEnclosureCommand("Barn", 10, "Cow", farmer.getId()));
        Enclosure enclosure = enclosureQueryService.handle(new GetEnclosureByIdQuery(enclosureId))
                .orElseThrow(() -> fail("Enclosure creation failed"));
        this.token = signInResult.getRight();
        this.enclosureId = enclosure.getId();
    }

    @Test
    void postAnimal() throws Exception {
        // Arrange
        CreateAnimalResource resource = new CreateAnimalResource("Bella", 3, "Cow", "Holstein", false, 450.0f, "HEALTHY", enclosureId);
        // Act
        mockMvc.perform(post("/api/v1/animals")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Bella"))
                .andExpect(jsonPath("$.age").value(3))
                .andExpect(jsonPath("$.species").value("Cow"))
                .andExpect(jsonPath("$.breed").value("Holstein"))
                .andExpect(jsonPath("$.gender").value(false))
                .andExpect(jsonPath("$.weight").value(450.0))
                .andExpect(jsonPath("$.health").value("HEALTHY"));
    }

    @Test
    void getAnimals() throws Exception {
        // Arrange
        CreateAnimalCommand commandOne = new CreateAnimalCommand("Luna", 2, "Sheep", "Merino", true, 70.5f, HealthStatus.HEALTHY.name(), enclosureId);
        CreateAnimalCommand commandTwo = new CreateAnimalCommand("Leo", 4, "Sheep", "Suffolk", false, 80.0f, HealthStatus.HEALTHY.name(), enclosureId);

        animalCommandService.handle(commandOne);
        animalCommandService.handle(commandTwo);

        // Act
        mockMvc.perform(get("/api/v1/animals")
                        .header("Authorization", "Bearer " + token))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Luna"))
                .andExpect(jsonPath("$[1].name").value("Leo"));
    }

    @Test
    void updateAnimal() throws Throwable {
        // Arrange
        CreateAnimalCommand command = new CreateAnimalCommand("Max", 4, "Horse", "Arabian", true, 500.0f, HealthStatus.HEALTHY.name(), enclosureId);
        Long animalId = animalCommandService.handle(command);

        Animal animal = animalQueryService.handle(new GetAnimalByIdQuery(animalId))
                .orElseThrow(() -> fail("Animal creation failed"));

        UpdateAnimalResource updateResource = new UpdateAnimalResource("Maximus", 5, "Horse", "Thoroughbred", true, 520.0f, "HEALTHY");

        // Act & Assert
        mockMvc.perform(put("/api/v1/animals/" + animal.getId())
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(updateResource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maximus"))
                .andExpect(jsonPath("$.age").value(5))
                .andExpect(jsonPath("$.breed").value("Thoroughbred"))
                .andExpect(jsonPath("$.weight").value(520.0));
    }

    @Test
    void deleteAnimal() throws Exception {
        // Arrange
        CreateAnimalCommand command = new CreateAnimalCommand("Toby", 1, "Pig", "Large White", true, 90.0f, HealthStatus.SICK.name(), enclosureId);
        Long animalId = animalCommandService.handle(command);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/animals/" + animalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        assertFalse(animalQueryService.handle(new GetAnimalByIdQuery(animalId)).isPresent(), "Animal should be deleted");
    }

    @Test
    void postAnimalWithInvalidToken() throws Exception {
        // Arrange
        CreateAnimalResource resource = new CreateAnimalResource("Test", 2, "Cow", "Holstein", false, 400.0f, HealthStatus.HEALTHY.name(), enclosureId);
        // Act
        mockMvc.perform(post("/api/v1/animals")
                        .contentType("application/json")
                        .header("Authorization", "Bearer invalid_token")
                        .content(objectMapper.writeValueAsString(resource)))
                // Assert
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAnimalsWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/animals")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateAnimalWithInvalidToken() throws Exception {
        UpdateAnimalResource update = new UpdateAnimalResource("Invalid", 1, "Pig", "Mixed", true, 80.0f, HealthStatus.SICK.name());
        mockMvc.perform(put("/api/v1/animals/1")
                        .contentType("application/json")
                        .header("Authorization", "Bearer invalid_token")
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteAnimalWithInvalidToken() throws Exception {
        mockMvc.perform(delete("/api/v1/animals/1")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }
}