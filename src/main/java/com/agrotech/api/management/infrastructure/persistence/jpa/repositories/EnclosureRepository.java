package com.agrotech.api.management.infrastructure.persistence.jpa.repositories;

import com.agrotech.api.management.infrastructure.persistence.jpa.entities.EnclosureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnclosureRepository extends JpaRepository<EnclosureEntity, Long> {
    List<EnclosureEntity> findAllByFarmer_Id(Long farmerId);
}
