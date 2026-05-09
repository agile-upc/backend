package com.agrotech.api.iam.interfaces.rest;

import com.agrotech.api.AgrotechApplication;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.commands.SignInCommand;
import com.agrotech.api.iam.domain.model.commands.SignUpCommand;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.services.UserCommandService;
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

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(classes = AgrotechApplication.class)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class UsersControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserCommandService userCommandService;


    private String token;
    private Long userId;

    @BeforeEach
    void setup() throws Throwable { // fail es un Throwable
        // Assert
        User user = userCommandService.handle(new SignUpCommand("testuser@example.com", "password", List.of(Role.getDefaultRole())))
                .orElseThrow(() -> fail("User creation failed"));
        this.token = userCommandService.handle(new SignInCommand("testuser@example.com", "password"))
                .orElseThrow(() -> fail("User sign-in failed")).getRight();
        this.userId = user.getId();
    }

    @Test
    void getAllUsers_withValidToken_returnsOk() throws Exception {
        // Act
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getUserById_withValidToken_returnsUser() throws Exception {
        // Act
        mockMvc.perform(get("/api/v1/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser@example.com"));
    }

    @Test
    void getUserById_withInvalidToken_returnsUnauthorized() throws Exception {
        // Act
        mockMvc.perform(get("/api/v1/users/" + userId)
                        .header("Authorization", "Bearer invalid_token"))
                // Assert
                .andExpect(status().isUnauthorized());
    }
}