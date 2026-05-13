package com.agrotech.api.profile.infrastructure.persistence.jpa.repository;

import com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.infrastructure.persistence.jpa.projection.AdvisorRecommendationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdvisorRepository extends JpaRepository<Advisor, Long> {
    Optional<Advisor> findByUser_Id(Long userId);

    @Query("""
            select
                advisor.id as advisorId,
                user.id as userId,
                advisor.rating as rating,
                profile.firstName as firstName,
                profile.lastName as lastName,
                profile.city as city,
                profile.country as country,
                profile.description as description,
                profile.photo as photo,
                profile.occupation as occupation,
                profile.experience as experience,
                (
                    select min(availableDate.scheduledDate)
                    from AvailableDate availableDate
                    where availableDate.advisor.id = advisor.id
                      and availableDate.status = :status
                      and availableDate.scheduledDate >= :today
                ) as nextAvailableDate
            from Advisor advisor
            join advisor.user user
            left join Profile profile on profile.user.id = user.id
            order by advisor.id
            """)
    List<AdvisorRecommendationProjection> findRecommendationInputs(
            @Param("status") AvailableDateStatus status,
            @Param("today") LocalDate today
    );
}
