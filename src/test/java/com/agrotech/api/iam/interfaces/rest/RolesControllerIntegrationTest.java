package com.agrotech.api.iam.interfaces.rest;

import com.agrotech.api.AgrotechApplication;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.commands.SignInCommand;
import com.agrotech.api.iam.domain.model.commands.SignUpCommand;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.services.UserCommandService;
import com.agrotech.api.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(classes = AgrotechApplication.class)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RolesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserCommandService userCommandService;

    private String token;

    @BeforeEach
    void setup() {
        // Assert

        // Create a test user
        User user = userCommandService.handle(
                new SignUpCommand("testuser@example.com", "password", List.of(Role.getDefaultRole()))
        ).orElseThrow();
        // Get token
        this.token = userCommandService.handle(
                new SignInCommand("testuser@example.com", "password")
        ).orElseThrow().getRight();
    }

    @Test
    void getAllRolesReturnsOkWithRoles() throws Exception {
        // Arrange
        // When the application starts, the roles are already populated in the database

        // Act
        mockMvc.perform(get("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

}
