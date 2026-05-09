package com.agrotech.api.profile.infrastructure.persistence.jpa.repositories;

import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {
    Optional<ProfileEntity> findByUser_Id(Long userId);
    @Query("SELECT p FROM ProfileEntity p WHERE p.occupation IS NOT NULL AND p.occupation <> ''")
    List<ProfileEntity> findAllAdvisorProfiles();
}
