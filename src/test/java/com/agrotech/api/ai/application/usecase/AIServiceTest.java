package com.agrotech.api.ai.application.usecase;

import com.agrotech.api.ai.infrastructure.web.dto.AIRecommendationRequestDto;
import com.agrotech.api.ai.infrastructure.web.dto.AIRecommendationStatus;
import com.agrotech.api.iam.application.usecase.AuthenticatedUserService;
import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import com.agrotech.api.iam.domain.model.User;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.profile.application.usecase.ProfileService;
import com.agrotech.api.profile.domain.model.Profile;
import com.agrotech.api.profile.infrastructure.persistence.jpa.projection.AdvisorRecommendationProjection;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repository.AdvisorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AIServiceTest {
    private static final Instant BASE_INSTANT = Instant.parse("2026-05-12T10:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 5, 12);

    private final AdvisorRepository advisorRepository = mock(AdvisorRepository.class);
    private final ProfileService profileService = mock(ProfileService.class);
    private final AuthenticatedUserService authenticatedUserService = mock(AuthenticatedUserService.class);

    private MutableClock clock;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(BASE_INSTANT, ZoneOffset.UTC);
    }

    @Test
    void recommendAdvisorsReturnsReadyWithSessionMetadataAndFallback() {
        AIService aiService = createService(null);
        mockFarmerProfile();
        when(advisorRepository.findRecommendationInputs(eq(com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus.AVAILABLE), eq(TODAY)))
                .thenReturn(List.of(
                        projection(1L, 200L, "Ana", "Lopez", "Cusco", "Peru", "Suelos", "Especialista en fertilizacion y suelos", 8, "4.80", TODAY.plusDays(1)),
                        projection(2L, 201L, "Bruno", "Rojas", "Lima", "Peru", "Riego", "Asesor en riego tecnificado", 4, "4.10", TODAY.plusDays(3))
                ));

        var response = aiService.recommendAdvisors(new AIRecommendationRequestDto(
                "Necesito ayuda con la fertilizacion del suelo de mi cultivo",
                null
        ));

        assertEquals(AIRecommendationStatus.READY, response.status());
        assertEquals(1L, response.selectedAdvisorId());
        assertEquals(2, response.matches().size());
        assertNull(response.conversationId());
        assertEquals(0, response.questionsAsked());
        assertEquals(1, response.maxQuestions());
        assertTrue(response.usedFallback());
        assertNotNull(response.draftAppointmentMessage());
    }

    @Test
    void recommendAdvisorsUsesSingleClarificationAndCachedShortlist() {
        AIService aiService = createService(null);
        mockFarmerProfile();
        when(advisorRepository.findRecommendationInputs(eq(com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus.AVAILABLE), eq(TODAY)))
                .thenReturn(List.of(
                        projection(1L, 200L, "Ana", "Lopez", "Cusco", "Peru", "Suelos", "Especialista agricola integral", 6, "4.60", TODAY.plusDays(2)),
                        projection(2L, 201L, "Bruno", "Rojas", "Cusco", "Peru", "Cultivos", "Especialista agricola integral", 6, "4.50", TODAY.plusDays(3))
                ));

        var firstResponse = aiService.recommendAdvisors(new AIRecommendationRequestDto("ayuda", null));

        assertEquals(AIRecommendationStatus.NEEDS_MORE_INFO, firstResponse.status());
        assertNotNull(firstResponse.conversationId());
        assertEquals(1, firstResponse.questionsAsked());
        assertTrue(firstResponse.usedFallback());
        assertNull(firstResponse.selectedAdvisorId());

        var secondResponse = aiService.recommendAdvisors(new AIRecommendationRequestDto(
                "Es para mejorar el manejo de mi cultivo",
                firstResponse.conversationId()
        ));

        assertEquals(AIRecommendationStatus.READY, secondResponse.status());
        assertEquals(firstResponse.conversationId(), secondResponse.conversationId());
        assertEquals(1L, secondResponse.selectedAdvisorId());
        assertEquals(1, secondResponse.questionsAsked());
        assertTrue(secondResponse.usedFallback());

        verify(advisorRepository, times(1)).findRecommendationInputs(eq(com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus.AVAILABLE), eq(TODAY));
    }

    @Test
    void recommendAdvisorsExpiredConversationStartsFreshSession() {
        AIService aiService = createService(null);
        mockFarmerProfile();
        when(advisorRepository.findRecommendationInputs(eq(com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus.AVAILABLE), eq(TODAY)))
                .thenReturn(List.of(
                        projection(1L, 200L, "Ana", "Lopez", "Cusco", "Peru", "Suelos", "Especialista agricola integral", 6, "4.60", TODAY.plusDays(2)),
                        projection(2L, 201L, "Bruno", "Rojas", "Cusco", "Peru", "Cultivos", "Especialista agricola integral", 6, "4.50", TODAY.plusDays(3))
                ));

        var firstResponse = aiService.recommendAdvisors(new AIRecommendationRequestDto("ayuda", null));
        clock.advance(Duration.ofMinutes(16));

        var secondResponse = aiService.recommendAdvisors(new AIRecommendationRequestDto(
                "ayuda",
                firstResponse.conversationId()
        ));

        assertEquals(AIRecommendationStatus.NEEDS_MORE_INFO, secondResponse.status());
        assertNotNull(secondResponse.conversationId());
        assertNotEquals(firstResponse.conversationId(), secondResponse.conversationId());
        verify(advisorRepository, times(2)).findRecommendationInputs(eq(com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus.AVAILABLE), eq(TODAY));
    }

    @Test
    void recommendAdvisorsGeminiRateLimitReturnsDeterministicFallback() {
        AIService aiService = createService(prompt -> {
            throw new RuntimeException("429 quota exceeded");
        });
        mockFarmerProfile();
        when(advisorRepository.findRecommendationInputs(eq(com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus.AVAILABLE), eq(TODAY)))
                .thenReturn(List.of(
                        projection(1L, 200L, "Ana", "Lopez", "Cusco", "Peru", "Suelos", "Especialista en fertilizacion y suelos", 8, "4.80", TODAY.plusDays(1)),
                        projection(2L, 201L, "Bruno", "Rojas", "Lima", "Peru", "Riego", "Asesor en riego tecnificado", 4, "4.10", TODAY.plusDays(3))
                ));

        var response = aiService.recommendAdvisors(new AIRecommendationRequestDto(
                "Necesito ayuda con la fertilizacion del suelo de mi cultivo",
                null
        ));

        assertEquals(AIRecommendationStatus.READY, response.status());
        assertEquals(1L, response.selectedAdvisorId());
        assertTrue(response.usedFallback());
        assertNotNull(response.summary());
        assertNotNull(response.draftAppointmentMessage());
    }

    @Test
    void recommendAdvisorsUnknownConversationStartsFreshSession() {
        AIService aiService = createService(null);
        mockFarmerProfile();
        when(advisorRepository.findRecommendationInputs(eq(com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus.AVAILABLE), eq(TODAY)))
                .thenReturn(List.of(
                        projection(1L, 200L, "Ana", "Lopez", "Cusco", "Peru", "Suelos", "Especialista agricola integral", 6, "4.60", TODAY.plusDays(2))
                ));

        var response = aiService.recommendAdvisors(new AIRecommendationRequestDto(
                "Necesito apoyo con mis cultivos",
                "missing-session"
        ));

        assertEquals(AIRecommendationStatus.READY, response.status());
        verify(advisorRepository, times(1)).findRecommendationInputs(eq(com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus.AVAILABLE), eq(TODAY));
    }

    private AIService createService(AIService.GeminiGateway geminiGateway) {
        return new AIService(
                advisorRepository,
                profileService,
                authenticatedUserService,
                new ObjectMapper(),
                clock,
                Duration.ofMinutes(15),
                geminiGateway
        );
    }

    private void mockFarmerProfile() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(
                new AuthenticatedUser(100L, 10L, "farmer@test.com", UserRole.FARMER, 50L, null)
        );
        when(profileService.getCurrentProfile()).thenReturn(
                Profile.builder()
                        .id(10L)
                        .user(User.builder().id(100L).username("farmer@test.com").password("secret").role(UserRole.FARMER).build())
                        .firstName("Lucia")
                        .lastName("Quispe")
                        .city("Cusco")
                        .country("Peru")
                        .build()
        );
    }

    private AdvisorRecommendationProjection projection(
            Long advisorId,
            Long userId,
            String firstName,
            String lastName,
            String city,
            String country,
            String occupation,
            String description,
            Integer experience,
            String rating,
            LocalDate nextAvailableDate
    ) {
        return new AdvisorRecommendationProjection() {
            @Override
            public Long getAdvisorId() {
                return advisorId;
            }

            @Override
            public Long getUserId() {
                return userId;
            }

            @Override
            public BigDecimal getRating() {
                return new BigDecimal(rating);
            }

            @Override
            public String getFirstName() {
                return firstName;
            }

            @Override
            public String getLastName() {
                return lastName;
            }

            @Override
            public String getCity() {
                return city;
            }

            @Override
            public String getCountry() {
                return country;
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public String getPhoto() {
                return null;
            }

            @Override
            public String getOccupation() {
                return occupation;
            }

            @Override
            public Integer getExperience() {
                return experience;
            }

            @Override
            public LocalDate getNextAvailableDate() {
                return nextAvailableDate;
            }
        };
    }

    private static final class MutableClock extends Clock {
        private Instant instant;
        private final ZoneOffset zoneOffset;

        private MutableClock(Instant instant, ZoneOffset zoneOffset) {
            this.instant = instant;
            this.zoneOffset = zoneOffset;
        }

        @Override
        public ZoneOffset getZone() {
            return zoneOffset;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return Clock.fixed(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
