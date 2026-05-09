package com.agrotech.api.management.infrastructure.persistence.jpa.repositories;

import com.agrotech.api.management.infrastructure.persistence.jpa.entities.AnimalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimalRepository extends JpaRepository<AnimalEntity, Long> {
    List<AnimalEntity> findAllByEnclosure_Id(Long enclosureId);
}
