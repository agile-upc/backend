package com.agrotech.api.ai.application.usecase;

import com.agrotech.api.ai.infrastructure.web.dto.AIRecommendationRequestDto;
import com.agrotech.api.ai.infrastructure.web.dto.AIRecommendationStatus;
import com.agrotech.api.appointment.domain.model.AvailableDate;
import com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repository.AvailableDateRepository;
import com.agrotech.api.iam.application.usecase.AuthenticatedUserService;
import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import com.agrotech.api.iam.domain.model.User;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.profile.application.usecase.ProfileService;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.domain.model.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AIServiceTest {
    private final AvailableDateRepository availableDateRepository = mock(AvailableDateRepository.class);
    private final ProfileService profileService = mock(ProfileService.class);
    private final AuthenticatedUserService authenticatedUserService = mock(AuthenticatedUserService.class);

    private AIService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AIService(
                availableDateRepository,
                profileService,
                authenticatedUserService,
                new ObjectMapper()
        );
    }

    @Test
    void recommendAdvisorsReturnsReadyWithDeterministicFallback() {
        Profile farmerProfile = profile(10L, 100L, "Lucia", "Quispe", "Cusco", "Peru", null, null, 0);
        when(authenticatedUserService.getCurrentUser()).thenReturn(
                new AuthenticatedUser(100L, 10L, "farmer@test.com", UserRole.FARMER, 50L, null)
        );
        when(profileService.getCurrentProfile()).thenReturn(farmerProfile);

        Profile bestProfile = profile(20L, 200L, "Ana", "Lopez", "Cusco", "Peru", "Suelos", "Especialista en fertilizacion y suelos", 8);
        Profile secondProfile = profile(21L, 201L, "Bruno", "Rojas", "Lima", "Peru", "Riego", "Asesor en riego tecnificado", 4);
        Advisor bestAdvisor = advisor(1L, 200L, "4.80");
        Advisor secondAdvisor = advisor(2L, 201L, "4.10");

        when(profileService.getAdvisorProfiles()).thenReturn(List.of(bestProfile, secondProfile));
        when(profileService.getAdvisorByUserId(200L)).thenReturn(bestAdvisor);
        when(profileService.getAdvisorByUserId(201L)).thenReturn(secondAdvisor);
        when(availableDateRepository.findByStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscStartTimeAsc(
                AvailableDateStatus.AVAILABLE,
                LocalDate.now()
        )).thenReturn(List.of(
                availableDate(1L, bestAdvisor, LocalDate.now().plusDays(1), "09:00", "10:00"),
                availableDate(2L, secondAdvisor, LocalDate.now().plusDays(3), "11:00", "12:00")
        ));

        var response = aiService.recommendAdvisors(new AIRecommendationRequestDto(
                "Necesito ayuda con la fertilizacion del suelo de mi cultivo"
        ));

        assertEquals(AIRecommendationStatus.READY, response.status());
        assertEquals(1L, response.selectedAdvisorId());
        assertEquals(2, response.matches().size());
        assertTrue(response.matches().getFirst().why().contains("misma ciudad"));
        assertNotNull(response.draftAppointmentMessage());
        assertTrue(response.draftAppointmentMessage().contains("fertilizacion del suelo"));
    }

    @Test
    void recommendAdvisorsReturnsNeedsMoreInfoForVagueRequest() {
        Profile farmerProfile = profile(10L, 100L, "Lucia", "Quispe", "Cusco", "Peru", null, null, 0);
        when(authenticatedUserService.getCurrentUser()).thenReturn(
                new AuthenticatedUser(100L, 10L, "farmer@test.com", UserRole.FARMER, 50L, null)
        );
        when(profileService.getCurrentProfile()).thenReturn(farmerProfile);

        Profile profile = profile(20L, 200L, "Ana", "Lopez", "Cusco", "Peru", "Suelos", "Especialista en fertilizacion y suelos", 8);
        Advisor advisor = advisor(1L, 200L, "4.80");

        when(profileService.getAdvisorProfiles()).thenReturn(List.of(profile));
        when(profileService.getAdvisorByUserId(200L)).thenReturn(advisor);
        when(availableDateRepository.findByStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscStartTimeAsc(
                AvailableDateStatus.AVAILABLE,
                LocalDate.now()
        )).thenReturn(List.of(
                availableDate(1L, advisor, LocalDate.now().plusDays(1), "09:00", "10:00")
        ));

        var response = aiService.recommendAdvisors(new AIRecommendationRequestDto("ayuda"));

        assertEquals(AIRecommendationStatus.NEEDS_MORE_INFO, response.status());
        assertNull(response.selectedAdvisorId());
        assertNotNull(response.clarifyingQuestion());
        assertEquals(1, response.matches().size());
    }

    @Test
    void recommendAdvisorsReturnsUnavailableWhenNoAdvisorsExist() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(
                new AuthenticatedUser(100L, 10L, "farmer@test.com", UserRole.FARMER, 50L, null)
        );
        when(profileService.getCurrentProfile()).thenReturn(
                profile(10L, 100L, "Lucia", "Quispe", "Cusco", "Peru", null, null, 0)
        );
        when(profileService.getAdvisorProfiles()).thenReturn(List.of());
        when(availableDateRepository.findByStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscStartTimeAsc(
                AvailableDateStatus.AVAILABLE,
                LocalDate.now()
        )).thenReturn(List.of());

        var response = aiService.recommendAdvisors(new AIRecommendationRequestDto(
                "Necesito orientacion para mis cultivos"
        ));

        assertEquals(AIRecommendationStatus.UNAVAILABLE, response.status());
        assertNull(response.selectedAdvisorId());
        assertTrue(response.matches().isEmpty());
    }

    private Profile profile(
            Long profileId,
            Long userId,
            String firstName,
            String lastName,
            String city,
            String country,
            String occupation,
            String description,
            int experience
    ) {
        return Profile.builder()
                .id(profileId)
                .user(User.builder().id(userId).username("user" + userId + "@test.com").password("secret").role(UserRole.ADVISOR).build())
                .firstName(firstName)
                .lastName(lastName)
                .city(city)
                .country(country)
                .occupation(occupation)
                .description(description)
                .experience(experience)
                .build();
    }

    private Advisor advisor(Long advisorId, Long userId, String rating) {
        return Advisor.builder()
                .id(advisorId)
                .user(User.builder().id(userId).username("advisor" + userId + "@test.com").password("secret").role(UserRole.ADVISOR).build())
                .rating(new BigDecimal(rating))
                .build();
    }

    private AvailableDate availableDate(Long id, Advisor advisor, LocalDate date, String startTime, String endTime) {
        return AvailableDate.builder()
                .id(id)
                .advisor(advisor)
                .scheduledDate(date)
                .startTime(startTime)
                .endTime(endTime)
                .status(AvailableDateStatus.AVAILABLE)
                .build();
    }
}
