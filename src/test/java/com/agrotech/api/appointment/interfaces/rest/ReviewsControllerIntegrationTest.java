package com.agrotech.api.appointment.interfaces.rest;

import com.agrotech.api.AgrotechApplication;
import com.agrotech.api.appointment.domain.model.commands.CreateAvailableDateCommand;
import com.agrotech.api.appointment.domain.services.AvailableDateCommandService;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.commands.SignInCommand;
import com.agrotech.api.iam.domain.model.commands.SignUpCommand;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.services.UserCommandService;
import com.agrotech.api.profile.domain.model.commands.CreateFarmerCommand;
import com.agrotech.api.profile.domain.model.commands.CreateAdvisorCommand;
import com.agrotech.api.profile.domain.model.commands.CreateProfileCommand;
import com.agrotech.api.profile.domain.services.FarmerCommandService;
import com.agrotech.api.profile.domain.services.AdvisorCommandService;
import com.agrotech.api.appointment.interfaces.rest.resources.CreateReviewResource;
import com.agrotech.api.appointment.interfaces.rest.resources.UpdateReviewResource;
import com.agrotech.api.profile.domain.services.ProfileCommandService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(classes = AgrotechApplication.class)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ReviewsControllerIntegrationTest {

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
        // Crear usuario para Farmer
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
    void postReview() throws Exception {
        // Crear recurso para Review con el advisorId correcto
        CreateReviewResource resource = new CreateReviewResource(advisorId, farmerId, "Excelente servicio", 5);

        // Realizar solicitud POST
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.advisorId").value(advisorId))
                .andExpect(jsonPath("$.farmerId").value(farmerId))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Excelente servicio"));
    }

    @Test
    void getReviews() throws Exception {
        // Crear una Review
        CreateReviewResource resource = new CreateReviewResource(advisorId, farmerId, "Buen servicio", 5);
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated());

        // Realizar solicitud GET
        mockMvc.perform(get("/api/v1/reviews")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].farmerId").value(farmerId))
                .andExpect(jsonPath("$[0].advisorId").value(advisorId));
    }

    @Test
    void getReviewById() throws Exception {
        // Crear una Review
        CreateReviewResource resource = new CreateReviewResource(advisorId, farmerId, "Servicio aceptable", 3);
        String response = mockMvc.perform(post("/api/v1/reviews")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long reviewId = JsonPath.parse(response).read("$.id", Long.class);

        // Realizar solicitud GET por ID
        mockMvc.perform(get("/api/v1/reviews/" + reviewId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId));
    }

    @Test
    void updateReview() throws Exception {
        // Crear una Review
        CreateReviewResource resource = new CreateReviewResource(advisorId, farmerId, "Servicio regular", 2);
        String response = mockMvc.perform(post("/api/v1/reviews")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long reviewId = JsonPath.parse(response).read("$.id", Long.class);

        // Crear recurso de actualización
        UpdateReviewResource updateResource = new UpdateReviewResource("Servicio mejorado", 4);

        // Realizar solicitud PUT
        mockMvc.perform(put("/api/v1/reviews/" + reviewId)
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(updateResource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Servicio mejorado"));
    }

    @Test
    void deleteReview() throws Exception {
        // Crear una Review
        CreateReviewResource resource = new CreateReviewResource(advisorId, farmerId, "Mala experiencia", 5);
        String response = mockMvc.perform(post("/api/v1/reviews")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long reviewId = JsonPath.parse(response).read("$.id", Long.class);

        // Realizar solicitud DELETE
        mockMvc.perform(delete("/api/v1/reviews/" + reviewId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Review with id " + reviewId + " deleted successfully"));
    }
}