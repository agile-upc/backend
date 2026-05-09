package com.agrotech.api.profile.infrastructure.persistence.jpa.repositories;

import com.agrotech.api.profile.infrastructure.persistence.jpa.entities.FarmerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FarmerRepository extends JpaRepository<FarmerEntity, Long> {
    Optional<FarmerEntity> findByUser_Id(Long userId);
}
