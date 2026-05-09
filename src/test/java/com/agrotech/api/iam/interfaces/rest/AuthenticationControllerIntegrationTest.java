package com.agrotech.api.iam.interfaces.rest;

import com.agrotech.api.AgrotechApplication;
import com.agrotech.api.iam.interfaces.rest.resources.SignInResource;
import com.agrotech.api.iam.interfaces.rest.resources.SignUpResource;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(classes = AgrotechApplication.class)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AuthenticationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void signUpWithValidDataReturnsCreated() throws Exception {
        // Arrange
        SignUpResource request = new SignUpResource(
                "user_" + UUID.randomUUID() + "@test.com", "password", List.of("ROLE_USER")
        );
        // Act
        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(request.username()));
    }

    @Test
    void signUpWithExistingEmailReturnsBadRequest() throws Exception {
        // Arrange
        String username = "existinguser@test.com";

        // Act
        SignUpResource request = new SignUpResource(username, "password", List.of("ROLE_USER"));
        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second sign-up with same username
        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Assert
                .andExpect(status().isBadRequest());
    }

    @Test
    void signInWithCorrectCredentialsReturnsOk() throws Exception {
        // Arrange
        String username = "validuser@test.com";
        String password = "password";

        // Act
        SignUpResource signUp = new SignUpResource(username, password, List.of("ROLE_USER"));
        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUp)))
                .andExpect(status().isCreated());

        SignInResource signIn = new SignInResource(username, password);
        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signIn)))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void signInWithInvalidCredentialsReturnsBadRequest() throws Exception {
        // Arrange
        SignInResource signIn = new SignInResource("nonexistent@test.com", "wrongpassword");
        // Act
        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signIn)))
                // Assert
                .andExpect(status().isBadRequest());
    }
}
