package com.agrotech.api.ai.application.usecase;

import com.agrotech.api.ai.infrastructure.web.dto.AIRecommendationRequestDto;
import com.agrotech.api.ai.infrastructure.web.dto.AIRecommendationStatus;
import com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AIServiceRecommendationConsistencyTest {
    private static final Instant BASE_INSTANT = Instant.parse("2026-05-13T10:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 5, 13);

    private final AdvisorRepository advisorRepository = mock(AdvisorRepository.class);
    private final ProfileService profileService = mock(ProfileService.class);
    private final AuthenticatedUserService authenticatedUserService = mock(AuthenticatedUserService.class);

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(BASE_INSTANT, ZoneOffset.UTC);
        when(authenticatedUserService.getCurrentUser()).thenReturn(
                new AuthenticatedUser(100L, 10L, "farmer@test.com", UserRole.FARMER, 50L, null)
        );
        when(profileService.getCurrentProfile()).thenReturn(
                Profile.builder()
                        .id(10L)
                        .user(User.builder().id(100L).username("farmer@test.com").password("secret").role(UserRole.FARMER).build())
                        .firstName("Lucia")
                        .lastName("Quispe")
                        .city("Iquitos")
                        .country("Peru")
                        .build()
        );
    }

    @Test
    void readyResponseKeepsSummaryAndMessageAlignedWithSelectedAdvisor() {
        when(advisorRepository.findRecommendationInputs(eq(AvailableDateStatus.AVAILABLE), eq(TODAY)))
                .thenReturn(List.of(
                        projection(7L, "Claudia", "Valdez", "Iquitos", "Peru", "Consultor Ambiental", 13, "2.5", TODAY.plusDays(1)),
                        projection(4L, "Carlos", "Gonzales", "Lima", "Peru", "Especialista en Riego", 3, "4.0", TODAY.plusDays(1)),
                        projection(1L, "Lucia", "Salazar", "Lima", "Peru", "Ingeniero en Agroindustria", 8, "2.0", TODAY.plusDays(1))
                ));

        AIService service = new AIService(
                advisorRepository,
                profileService,
                authenticatedUserService,
                new ObjectMapper(),
                clock,
                Duration.ofMinutes(15),
                prompt -> """
                        {"summary":"Se ha seleccionado a Lucia Salazar para revisar el caso.","clarifyingQuestion":null,"draftAppointmentMessage":"Estimada Lucia, necesito revisar mi cultivo de maiz."}
                        """
        );

        var response = service.recommendAdvisors(new AIRecommendationRequestDto(
                "Tengo un problema en mi cultivo de maiz, varias plantas se estan secando antes de tiempo.",
                null
        ));

        assertEquals(AIRecommendationStatus.READY, response.status());
        assertEquals(7L, response.selectedAdvisorId());
        assertEquals(7L, response.matches().getFirst().advisorId());
        assertTrue(response.summary().contains("Claudia Valdez"));
        assertTrue(response.draftAppointmentMessage().startsWith("Hola Claudia Valdez,"));
    }

    private AdvisorRecommendationProjection projection(
            Long advisorId,
            String firstName,
            String lastName,
            String city,
            String country,
            String occupation,
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
                return advisorId + 100;
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
                return "Asesor agricola disponible";
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
}
