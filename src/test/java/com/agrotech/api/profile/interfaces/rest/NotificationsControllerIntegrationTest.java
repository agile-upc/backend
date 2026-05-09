package com.agrotech.api.profile.interfaces.rest;

import com.agrotech.api.AgrotechApplication;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.commands.SignInCommand;
import com.agrotech.api.iam.domain.model.commands.SignUpCommand;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.services.UserCommandService;
import com.agrotech.api.profile.domain.model.commands.CreateNotificationCommand;
import com.agrotech.api.profile.domain.services.NotificationCommandService;
import com.agrotech.api.profile.interfaces.rest.resources.CreateNotificationResource;
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

import java.util.Date;
import java.util.List;


import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(classes = AgrotechApplication.class)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class NotificationsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserCommandService userCommandService;

    @Autowired
    private NotificationCommandService notificationCommandService;



    private String token;
    private Long userId;

    @BeforeEach
    void setup() throws Throwable {
        // Crear usuario
        User user = userCommandService.handle(new SignUpCommand("testuser@example.com", "password", List.of(Role.getDefaultRole())))
                .orElseThrow(() -> fail("User creation failed"));
        ImmutablePair<User, String> signInResult = userCommandService.handle(new SignInCommand("testuser@example.com", "password"))
                .orElseThrow(() -> fail("User sign-in failed"));
        this.token = signInResult.getRight();
        this.userId = user.getId();
    }

    @Test
    void getAllNotifications() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getNotificationsByUserId() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/" + userId + "/user")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getNotificationById() throws Exception {
        // Crear notificación
        Long notificationId = notificationCommandService.handle(new CreateNotificationCommand(userId, "Test Notification", "This is a test",new Date()));

        mockMvc.perform(get("/api/v1/notifications/" + notificationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notificationId));
    }

    @Test
    void createNotification() throws Exception {
        CreateNotificationResource resource = new CreateNotificationResource(userId, "New Notification", "Notification content",new Date());

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Notification"))
                .andExpect(jsonPath("$.message").value("Notification content"));
    }

    @Test
    void deleteNotification() throws Exception {
        // Crear notificación
        Long notificationId = notificationCommandService.handle(new CreateNotificationCommand(userId, "Test Notification", "This is a test",new Date()));

        mockMvc.perform(delete("/api/v1/notifications/" + notificationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Notification with id: " + notificationId + " deleted successfully"));
    }
}