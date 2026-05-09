package com.agrotech.api.appointment.interfaces.rest;

import com.agrotech.api.AgrotechApplication;
import com.agrotech.api.appointment.interfaces.rest.resources.UpdateAvailableDateResource;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.commands.SignInCommand;
import com.agrotech.api.iam.domain.model.commands.SignUpCommand;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.services.UserCommandService;
import com.agrotech.api.profile.domain.model.commands.CreateAdvisorCommand;
import com.agrotech.api.profile.domain.services.AdvisorCommandService;
import com.agrotech.api.appointment.domain.model.commands.CreateAvailableDateCommand;
import com.agrotech.api.appointment.domain.services.AvailableDateCommandService;
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
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(classes = AgrotechApplication.class)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AvailableDatesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserCommandService userCommandService;

    @Autowired
    private AdvisorCommandService advisorCommandService;

    @Autowired
    private AvailableDateCommandService availableDateCommandService;

    private String token;
    private Long advisorId;

    @BeforeEach
    void setup() throws Throwable {
        // Crear usuario para Advisor
        User advisorUser = userCommandService.handle(new SignUpCommand("advisor@example.com", "password", List.of(Role.getDefaultRole())))
                .orElseThrow(() -> fail("User creation failed"));
        ImmutablePair<User, String> advisorSignInResult = userCommandService.handle(new SignInCommand("advisor@example.com", "password"))
                .orElseThrow(() -> fail("User sign-in failed"));
        this.token = advisorSignInResult.getRight();

        // Crear Advisor
        this.advisorId = advisorCommandService.handle(new CreateAdvisorCommand(advisorUser.getId()), advisorUser);
    }

    @Test
    void postAvailableDate() throws Exception {
        // Crear recurso para AvailableDate
        var resource = new CreateAvailableDateCommand(advisorId, LocalDate.parse("2028-06-20", DateTimeFormatter.ISO_LOCAL_DATE), "10:00", "11:00");

        // Realizar solicitud POST
        mockMvc.perform(post("/api/v1/available_dates")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.advisorId").value(advisorId))
                .andExpect(jsonPath("$.scheduledDate").value("2028-06-20"))
                .andExpect(jsonPath("$.startTime").value("10:00"))
                .andExpect(jsonPath("$.endTime").value("11:00"));
    }

    @Test
    void getAvailableDates() throws Exception {
        // Crear AvailableDate
        availableDateCommandService.handle(new CreateAvailableDateCommand(advisorId, LocalDate.parse("2028-06-20", DateTimeFormatter.ISO_LOCAL_DATE), "10:00", "11:00"));

        // Realizar solicitud GET
        mockMvc.perform(get("/api/v1/available_dates")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.advisorId == %d)]", advisorId).exists());
    }

    @Test
    void getAvailableDateById() throws Exception {
        // Crear AvailableDate
        Long availableDateId = availableDateCommandService.handle(new CreateAvailableDateCommand(advisorId, LocalDate.parse("2028-06-20", DateTimeFormatter.ISO_LOCAL_DATE), "10:00", "11:00"));

        // Realizar solicitud GET por ID
        mockMvc.perform(get("/api/v1/available_dates/" + availableDateId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(availableDateId));
    }

    @Test
    void updateAvailableDate() throws Exception {
        // Crear AvailableDate
        Long availableDateId = availableDateCommandService.handle(new CreateAvailableDateCommand(advisorId, LocalDate.parse("2028-06-20", DateTimeFormatter.ISO_LOCAL_DATE), "10:00", "11:00"));

        // Crear recurso de actualización
        var updateResource = new UpdateAvailableDateResource(LocalDate.parse("2028-06-20"), "11:00", "12:00");

        // Realizar solicitud PUT
        mockMvc.perform(put("/api/v1/available_dates/" + availableDateId)
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(updateResource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("11:00"))
                .andExpect(jsonPath("$.endTime").value("12:00"));
    }

    @Test
    void deleteAvailableDate() throws Exception {
        // Crear AvailableDate
        Long availableDateId = availableDateCommandService.handle(new CreateAvailableDateCommand(advisorId, LocalDate.parse("2028-06-20", DateTimeFormatter.ISO_LOCAL_DATE), "10:00", "11:00"));

        // Realizar solicitud DELETE
        mockMvc.perform(delete("/api/v1/available_dates/" + availableDateId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Available Date with id " + availableDateId + " deleted successfully"));
    }
}