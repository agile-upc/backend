package com.agrotech.api.profile.infrastructure.persistence.jpa.repository;

import com.agrotech.api.profile.domain.model.Advisor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvisorRepository extends JpaRepository<Advisor, Long> {
    Optional<Advisor> findByUser_Id(Long userId);
}
