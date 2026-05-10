package com.agrotech.api.profile.infrastructure.persistence.jpa.repository;

import com.agrotech.api.profile.domain.model.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {
    Optional<Farmer> findByUser_Id(Long userId);
}
