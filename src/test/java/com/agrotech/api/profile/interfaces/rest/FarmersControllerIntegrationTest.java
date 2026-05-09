package com.agrotech.api.profile.interfaces.rest;

import com.agrotech.api.AgrotechApplication;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.commands.SignInCommand;
import com.agrotech.api.iam.domain.model.commands.SignUpCommand;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.services.UserCommandService;
import com.agrotech.api.profile.domain.model.commands.CreateFarmerCommand;
import com.agrotech.api.profile.domain.services.FarmerCommandService;
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
class FarmersControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    @Autowired
        private UserCommandService userCommandService;

    @Autowired
    private FarmerCommandService farmerCommandService;

    private String token;
    private Long farmerId;

    private Long UserFarmerId;

    @BeforeEach
    void setup() throws Throwable {
        // Crear usuario para Farmer
        User farmerUser = userCommandService.handle(new SignUpCommand("farmer@example.com", "password", List.of(Role.getDefaultRole())))
                .orElseThrow(() -> fail("User creation failed"));
        ImmutablePair<User, String> farmerSignInResult = userCommandService.handle(new SignInCommand("farmer@example.com", "password"))
                .orElseThrow(() -> fail("User sign-in failed"));
        this.token = farmerSignInResult.getRight();

        // Crear Farmer
        this.farmerId = farmerCommandService.handle(new CreateFarmerCommand(farmerUser.getId()), farmerUser);
        if (this.farmerId == null) {
            fail("Farmer creation failed");
        }

        UserFarmerId=farmerUser.getId();
    }

    @Test
    void getAllFarmers() throws Exception {
        mockMvc.perform(get("/api/v1/farmers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getFarmerById() throws Exception {
        mockMvc.perform(get("/api/v1/farmers/" + farmerId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(farmerId));
    }

    @Test
    void getFarmerByUserId() throws Exception {
        mockMvc.perform(get("/api/v1/farmers/" + UserFarmerId + "/user")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(farmerId));
    }

    @Test
    void deleteFarmer() throws Exception {
        mockMvc.perform(delete("/api/v1/farmers/" + farmerId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Farmer with id " + farmerId + " deleted successfully"));
    }
}