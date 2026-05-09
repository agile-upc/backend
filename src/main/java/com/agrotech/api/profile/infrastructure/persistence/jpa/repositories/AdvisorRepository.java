package com.agrotech.api.profile.infrastructure.persistence.jpa.repositories;

import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.AdvisorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvisorRepository extends JpaRepository<AdvisorEntity, Long> {
    Optional<AdvisorEntity> findByUser_Id(Long userId);
}
