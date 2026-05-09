package com.agrotech.api.management.infrastructure.persistence.jpa.repositories;

import com.agrotech.api.management.infrastructure.persistence.jpa.entities.CropEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CropRepository extends JpaRepository<CropEntity, Long> {
    List<CropEntity> findAllByFarmer_Id(Long farmerId);
}
