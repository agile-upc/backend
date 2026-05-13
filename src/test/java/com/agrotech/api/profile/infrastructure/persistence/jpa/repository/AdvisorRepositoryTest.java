package com.agrotech.api.profile.infrastructure.persistence.jpa.repository;

import com.agrotech.api.appointment.domain.model.AvailableDate;
import com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus;
import com.agrotech.api.iam.domain.model.User;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.domain.model.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:advisor-repo-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false;NON_KEYWORDS=USER,REVIEW",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdvisorRepositoryTest {
    @Autowired
    private AdvisorRepository advisorRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findRecommendationInputsReturnsProfileAndNextAvailableDate() {
        LocalDate today = LocalDate.of(2026, 5, 12);

        Advisor firstAdvisor = persistAdvisor(
                1,
                "ana@test.com",
                "Ana",
                "Lopez",
                "Cusco",
                "Peru",
                "Suelos",
                "Especialista en fertilizacion",
                8,
                new BigDecimal("4.80")
        );
        persistAvailableDate(firstAdvisor, today.minusDays(1), AvailableDateStatus.AVAILABLE, "09:00", "10:00");
        persistAvailableDate(firstAdvisor, today.plusDays(5), AvailableDateStatus.AVAILABLE, "09:00", "10:00");
        persistAvailableDate(firstAdvisor, today.plusDays(2), AvailableDateStatus.AVAILABLE, "11:00", "12:00");
        persistAvailableDate(firstAdvisor, today.plusDays(1), AvailableDateStatus.UNAVAILABLE, "08:00", "09:00");

        Advisor secondAdvisor = persistAdvisor(
                2,
                "bruno@test.com",
                "Bruno",
                "Rojas",
                "Lima",
                "Peru",
                "Riego",
                "Asesor en riego",
                4,
                new BigDecimal("4.10")
        );
        persistAvailableDate(secondAdvisor, today.minusDays(3), AvailableDateStatus.AVAILABLE, "10:00", "11:00");
        persistAvailableDate(secondAdvisor, today.plusDays(4), AvailableDateStatus.UNAVAILABLE, "10:00", "11:00");

        entityManager.flush();

        List<com.agrotech.api.profile.infrastructure.persistence.jpa.projection.AdvisorRecommendationProjection> results =
                advisorRepository.findRecommendationInputs(AvailableDateStatus.AVAILABLE, today).stream()
                        .sorted(Comparator.comparing(com.agrotech.api.profile.infrastructure.persistence.jpa.projection.AdvisorRecommendationProjection::getAdvisorId))
                        .toList();

        assertEquals(2, results.size());

        var first = results.get(0);
        assertEquals(firstAdvisor.getId(), first.getAdvisorId());
        assertEquals("Ana", first.getFirstName());
        assertEquals("Lopez", first.getLastName());
        assertEquals("Cusco", first.getCity());
        assertEquals("Peru", first.getCountry());
        assertEquals("Suelos", first.getOccupation());
        assertEquals(8, first.getExperience());
        assertEquals(today.plusDays(2), first.getNextAvailableDate());
        assertNotNull(first.getRating());

        var second = results.get(1);
        assertEquals(secondAdvisor.getId(), second.getAdvisorId());
        assertEquals("Bruno", second.getFirstName());
        assertNull(second.getNextAvailableDate());
    }

    private Advisor persistAdvisor(
            int sequence,
            String username,
            String firstName,
            String lastName,
            String city,
            String country,
            String occupation,
            String description,
            int experience,
            BigDecimal rating
    ) {
        User user = entityManager.persistFlushFind(
                User.builder()
                        .username(username)
                        .password("secret")
                        .role(UserRole.ADVISOR)
                        .build()
        );

        entityManager.persist(
                Profile.builder()
                        .user(user)
                        .firstName(firstName)
                        .lastName(lastName)
                        .city(city)
                        .country(country)
                        .occupation(occupation)
                        .description(description)
                        .experience(experience)
                        .build()
        );

        return entityManager.persistFlushFind(
                Advisor.builder()
                        .user(user)
                        .rating(rating)
                        .build()
        );
    }

    private void persistAvailableDate(
            Advisor advisor,
            LocalDate scheduledDate,
            AvailableDateStatus status,
            String startTime,
            String endTime
    ) {
        entityManager.persist(
                AvailableDate.builder()
                        .advisor(advisor)
                        .scheduledDate(scheduledDate)
                        .startTime(startTime)
                        .endTime(endTime)
                        .status(status)
                        .build()
        );
    }
}
