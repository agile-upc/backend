package com.agrotech.api.post.interfaces.rest;

import com.agrotech.api.AgrotechApplication;
import com.agrotech.api.appointment.domain.model.commands.CreateAvailableDateCommand;
import com.agrotech.api.appointment.domain.services.AvailableDateCommandService;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.commands.SignInCommand;
import com.agrotech.api.iam.domain.model.commands.SignUpCommand;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.services.UserCommandService;
import com.agrotech.api.post.domain.model.commands.CreatePostCommand;
import com.agrotech.api.post.domain.services.PostCommandService;
import com.agrotech.api.post.interfaces.rest.resources.CreatePostResource;
import com.agrotech.api.post.interfaces.rest.resources.UpdatePostResource;
import com.agrotech.api.profile.domain.model.commands.CreateAdvisorCommand;
import com.agrotech.api.profile.domain.model.commands.CreateFarmerCommand;
import com.agrotech.api.profile.domain.model.commands.CreateProfileCommand;
import com.agrotech.api.profile.domain.services.AdvisorCommandService;
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

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("test")
@SpringBootTest(classes = AgrotechApplication.class)
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PostsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserCommandService userCommandService;

    @Autowired
    private AdvisorCommandService advisorCommandService;

    @Autowired
    private ProfileCommandService profileCommandService;

    @Autowired
    private AvailableDateCommandService availableDateCommandService;


    @Autowired
    private PostCommandService postCommandService;

    private String token;
    private Long advisorId;

    private Long availableDateId;
    @BeforeEach
    void setup() throws Throwable {
        // Crear usuario para advisor
        User advisorUser = userCommandService.handle(new SignUpCommand("advisor@example.com", "password", List.of(Role.getDefaultRole())))
                .orElseThrow(() -> fail("User creation failed"));
        ImmutablePair<User, String> advisorSignInResult = userCommandService.handle(new SignInCommand("advisor@example.com", "password"))
                .orElseThrow(() -> fail("User sign-in failed"));
        this.token = advisorSignInResult.getRight();

        // Crear Advisor
        this.advisorId = advisorCommandService.handle(new CreateAdvisorCommand(advisorUser.getId()), advisorUser);
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
    void createPost() throws Exception {
        // Crear recurso para el post
        CreatePostResource resource = new CreatePostResource(advisorId, "New Title", "New Description", "new-image.jpg");

        // Realizar solicitud POST
        mockMvc.perform(post("/api/v1/posts")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.advisorId").value(advisorId))
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    void getPosts() throws Exception {
        // Crear algunos posts
        postCommandService.handle(new CreatePostCommand(advisorId, "Title 1", "Description 1", "image1.jpg"));
        postCommandService.handle(new CreatePostCommand(advisorId, "Title 2", "Description 2", "image2.jpg"));

        // Realizar solicitud GET
        mockMvc.perform(get("/api/v1/posts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].advisorId").value(advisorId));
    }

    @Test
    void getPostById() throws Exception {
        // Crear un post
        Long postId = postCommandService.handle(new CreatePostCommand(advisorId, "Title", "Description", "image.jpg"));

        // Realizar solicitud GET por ID
        mockMvc.perform(get("/api/v1/posts/" + postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId));
    }


    @Test
    void updatePost() throws Exception {
        // Crear un post
        Long postId = postCommandService.handle(new CreatePostCommand(advisorId, "Old Title", "Old Description", "old-image.jpg"));

        // Crear recurso de actualización
        UpdatePostResource resource = new UpdatePostResource("Updated Title", "Updated Description", "updated-image.jpg");

        // Realizar solicitud PUT
        mockMvc.perform(put("/api/v1/posts/" + postId)
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void deletePost() throws Exception {
        // Crear un post
        Long postId = postCommandService.handle(new CreatePostCommand(advisorId, "Title", "Description", "image.jpg"));

        // Realizar solicitud DELETE
        mockMvc.perform(delete("/api/v1/posts/" + postId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Post with id " + postId + " successfully deleted"));
    }
}