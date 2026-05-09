package com.agrotech.api.appointment.interfaces.exceptions;

import com.agrotech.api.AgrotechApplication;
import com.agrotech.api.appointment.domain.model.commands.CreateAvailableDateCommand;
import com.agrotech.api.appointment.domain.services.AvailableDateCommandService;
import com.agrotech.api.appointment.interfaces.rest.resources.CreateReviewResource;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.commands.SignInCommand;
import com.agrotech.api.iam.domain.model.commands.SignUpCommand;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.services.UserCommandService;
import com.agrotech.api.profile.domain.model.commands.CreateAdvisorCommand;
import com.agrotech.api.profile.domain.model.commands.CreateFarmerCommand;
import com.agrotech.api.profile.domain.model.commands.CreateProfileCommand;
import com.agrotech.api.profile.domain.services.AdvisorCommandService;
import com.agrotech.api.profile.domain.services.FarmerCommandService;
import com.agrotech.api.profile.domain.services.ProfileCommandService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(classes = AgrotechApplication.class)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AppointmentExceptionsHandlerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserCommandService userCommandService;

    @Autowired
    private FarmerCommandService farmerCommandService;

    @Autowired
    private AdvisorCommandService advisorCommandService;

    @Autowired
    private ProfileCommandService profileCommandService;

    @Autowired
    private AvailableDateCommandService availableDateCommandService;

    private String token;
    private Long farmerId;
    private Long advisorId;

    private Long availableDateId;

    @BeforeEach
    void setup() throws Throwable {

        User farmerUser = userCommandService.handle(new SignUpCommand("farmer@example.com", "password", List.of(Role.getDefaultRole())))
                .orElseThrow(() -> fail("User creation failed"));
        ImmutablePair<User, String> farmerSignInResult = userCommandService.handle(new SignInCommand("farmer@example.com", "password"))
                .orElseThrow(() -> fail("User sign-in failed"));
        this.token = farmerSignInResult.getRight();
        this.farmerId = farmerCommandService.handle(new CreateFarmerCommand(farmerUser.getId()), farmerUser);
        profileCommandService.handle(new CreateProfileCommand(farmerUser.getId(), "Juan", "Pérez", "Lima", "Peru", LocalDate.of(1980, 5, 5), "Soy granjero", "photo.jpg", null, null));

        // Crear usuario para Advisor
        User advisorUser = userCommandService.handle(new SignUpCommand("advisor@example.com", "password", List.of(Role.getDefaultRole())))
                .orElseThrow(() -> fail("User creation failed"));
        this.advisorId = advisorCommandService.handle(new CreateAdvisorCommand(advisorUser.getId()), advisorUser);
        if (this.advisorId == null) {
            fail("Advisor creation failed");
        }
        System.out.println("Advisor ID creado: " + this.advisorId);

        // Crear perfil de Advisor
        profileCommandService.handle(new CreateProfileCommand(advisorUser.getId(), "Ana", "Gómez", "Madrid", "Spain", LocalDate.of(1975, 3, 15), "Asesora agrícola", "advisor_photo.jpg", "Asesor agricola", 15));

        // Crear AvailableDate
        this.availableDateId = availableDateCommandService.handle(new CreateAvailableDateCommand(advisorId, LocalDate.of(2026, 5, 5), "10:00", "11:00"));
        if (this.availableDateId == null) {
            fail("AvailableDate creation failed");
        }
    }


    @Test
    void handleAppointmentNotFoundException() throws Exception {
        mockMvc.perform(get("/api/v1/appointments/999")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Cambia el estado esperado a 404
                .andExpect(jsonPath("$.error").value("Appointment Not Found"))
                .andExpect(jsonPath("$.message").value("Appointment with id 999 not found"));
    }

    @Test
    void handleAvailableDateNotFoundException() throws Exception {
        mockMvc.perform(get("/api/v1/available_dates/999")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Available Date Not Found"))
                .andExpect(jsonPath("$.message").value("Available date not found with id: 999"));
    }


    @Test
    void handleReviewNotFoundException() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/999")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Review Not Found"))
                .andExpect(jsonPath("$.message").value("Review with id 999 not found"));
    }


    @Test
    void handleProfileNotFoundException() throws Exception {
        mockMvc.perform(get("/api/v1/profiles/999/user")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Profile Not Found"))
                .andExpect(jsonPath("$.message").value("Profile not found when creating notification for appointment."));
    }


    @Test
    void handleInvalidRatingException() throws Exception {
        CreateReviewResource resource = new CreateReviewResource(advisorId, farmerId, "Excelente servicio", 7);

        // Realizar solicitud POST
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isBadRequest()) // Esperar 400
                .andExpect(jsonPath("$.error").value("Invalid Rating"))
                .andExpect(jsonPath("$.message").value("Rating must be between 0 and 5, but was 7"));
    }


    @Test
    void handleInvalidDateException() throws Exception {

        var resource = new CreateAvailableDateCommand(advisorId, LocalDate.parse("2023-06-20", DateTimeFormatter.ISO_LOCAL_DATE), "11:00", "9:00");

        mockMvc.perform(post("/api/v1/available_dates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Date"))
                .andExpect(jsonPath("$.message").value("The date 20-06-23 is in the past. Please provide a valid date."));
    }

    @Test
    void handleInvalidTimeRangeException() throws Exception {
        var resource = new CreateAvailableDateCommand(advisorId, LocalDate.now(), "11:00", "10:00");


        mockMvc.perform(post("/api/v1/available_dates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Time Range"))
                .andExpect(jsonPath("$.message").value("Invalid time range: 11:00 to 10:00. Start time must be before end time."));
    }

    @Test
    void handleIncorrectTimeFormatException() throws Exception {

        var resource = new CreateAvailableDateCommand(advisorId, LocalDate.parse("2023-06-20", DateTimeFormatter.ISO_LOCAL_DATE), "11-AM", "9-AM");

        mockMvc.perform(post("/api/v1/available_dates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Date"))
                .andExpect(jsonPath("$.message").value("The date 20-06-23 is in the past. Please provide a valid date."));
    }
}