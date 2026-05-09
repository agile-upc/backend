package com.agrotech.api.profile.interfaces.rest;

import com.agrotech.api.AgrotechApplication;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.commands.SignInCommand;
import com.agrotech.api.iam.domain.model.commands.SignUpCommand;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.services.UserCommandService;
import com.agrotech.api.profile.domain.model.commands.CreateAdvisorCommand;
import com.agrotech.api.profile.domain.services.AdvisorCommandService;
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


import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(classes = AgrotechApplication.class)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AdvisorsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private UserCommandService userCommandService;

    @Autowired
    private AdvisorCommandService advisorCommandService;

    private String token;
    private Long advisorId;

    private Long UserAdvisorId;
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
        if (this.advisorId == null) {
            fail("Advisor creation failed");
        }
        UserAdvisorId= advisorUser.getId();


        // Verificar IDs
        System.out.println("Advisor ID: " + this.advisorId);
        System.out.println("User ID: " + advisorUser.getId());
    }

    @Test
    void getAllAdvisors() throws Exception {
        // Realizar solicitud GET
        mockMvc.perform(get("/api/v1/advisors")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAdvisorById() throws Exception {
        // Realizar solicitud GET por ID
        mockMvc.perform(get("/api/v1/advisors/" + advisorId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(advisorId));
    }

    @Test
    void getAdvisorByUserId() throws Exception {
        // Verificar que el userId es correcto
        System.out.println("Testing with User ID: " + advisorId);

        // Realizar solicitud GET por User ID
        mockMvc.perform(get("/api/v1/advisors/" + UserAdvisorId + "/user")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(advisorId));
    }

    @Test
    void deleteAdvisor() throws Exception {
        // Realizar solicitud DELETE
        mockMvc.perform(delete("/api/v1/advisors/" + advisorId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Advisor with id " + advisorId + " deleted successfully"));
    }
}