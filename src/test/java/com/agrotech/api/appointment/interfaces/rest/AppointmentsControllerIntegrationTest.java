package com.agrotech.api.appointment.interfaces.rest;

import com.agrotech.api.AgrotechApplication;
import com.agrotech.api.appointment.application.internal.outboundservices.acl.ExternalProfilesService;
import com.agrotech.api.appointment.interfaces.rest.resources.CreateAppointmentResource;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.commands.SignInCommand;
import com.agrotech.api.iam.domain.model.commands.SignUpCommand;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.services.UserCommandService;
import com.agrotech.api.profile.domain.model.aggregates.Profile;
import com.agrotech.api.profile.domain.model.commands.CreateAdvisorCommand;
import com.agrotech.api.profile.domain.model.commands.CreateFarmerCommand;
import com.agrotech.api.profile.domain.model.commands.CreateProfileCommand;
import com.agrotech.api.profile.domain.model.entities.Farmer;
import com.agrotech.api.profile.domain.model.queries.GetFarmerByIdQuery;
import com.agrotech.api.profile.domain.services.AdvisorCommandService;
import com.agrotech.api.profile.domain.services.FarmerCommandService;
import com.agrotech.api.profile.domain.services.FarmerQueryService;
import com.agrotech.api.appointment.domain.model.commands.CreateAvailableDateCommand;
import com.agrotech.api.appointment.domain.services.AvailableDateCommandService;
import com.agrotech.api.profile.domain.services.ProfileCommandService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("test")
@SpringBootTest(classes = AgrotechApplication.class)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AppointmentsControllerIntegrationTest {
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
    private Long availableDateId;

    @BeforeEach
    void setup() throws Throwable {
        // Crear usuario para Farmer
        User farmerUser = userCommandService.handle(new SignUpCommand("farmer@example.com", "password", List.of(Role.getDefaultRole())))
                .orElseThrow(() -> fail("User creation failed"));
        ImmutablePair<User, String> farmerSignInResult = userCommandService.handle(new SignInCommand("farmer@example.com", "password"))
                .orElseThrow(() -> fail("User sign-in failed"));
        // Obtener el token de acceso
        this.token = farmerSignInResult.getRight();
        // Crear Farmer
        this.farmerId = farmerCommandService.handle(new CreateFarmerCommand(farmerUser.getId()), farmerUser);
        // Crear perfil de Farmer
        profileCommandService.handle(new CreateProfileCommand(farmerUser.getId(), "Juan", "Pérez", "Lima", "Peru", LocalDate.of(1980, 5, 5), "Soy granjero", "photo.jpg", null, null));
        // Crear usuario para Advisor
        User advisorUser = userCommandService.handle(new SignUpCommand("advisor@example.com", "password", List.of(Role.getDefaultRole())))
                .orElseThrow(() -> fail("User creation failed"));
        // Crear Advisor
        Long advisorId = advisorCommandService.handle(new CreateAdvisorCommand(advisorUser.getId()), advisorUser);
        // Crear perfil de Advisor
        profileCommandService.handle(new CreateProfileCommand(advisorUser.getId(), "Ana", "Gómez", "Madrid", "Spain", LocalDate.of(1975, 3, 15), "Asesora agrícola", "advisor_photo.jpg", "Asesor agricola", 15));
        // Crear AvailableDate
        this.availableDateId = availableDateCommandService.handle(new CreateAvailableDateCommand(advisorId, LocalDate.of(2026, 5, 5), "10:00", "11:00"));

        // Validar que el AvailableDate fue creado
        if (this.availableDateId == null) {
            fail("AvailableDate creation failed");
        }
    }

    @Test
    void postAppointment() throws Exception {
        CreateAppointmentResource resource = new CreateAppointmentResource(farmerId, availableDateId, "Necesito asesoría");

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Necesito asesoría"));
    }

    @Test
    void getAppointmentById() throws Exception {
        CreateAppointmentResource resource = new CreateAppointmentResource(farmerId, availableDateId, "Necesito asesoría");

        String response = mockMvc.perform(post("/api/v1/appointments")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long appointmentId = JsonPath.parse(response).read("$.id", Long.class);

        mockMvc.perform(get("/api/v1/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId));
    }

    @Test
    void deleteAppointment() throws Exception {
        CreateAppointmentResource resource = new CreateAppointmentResource(farmerId, availableDateId, "Necesito asesoría");

        String response = mockMvc.perform(post("/api/v1/appointments")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long appointmentId = JsonPath.parse(response).read("$.id", Long.class);

        mockMvc.perform(delete("/api/v1/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Appointment with id " + appointmentId + " deleted successfully"));
    }
}