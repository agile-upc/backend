package com.agrotech.api.appointment.infrastructure.persistence.jpa.repositories;

import com.agrotech.api.appointment.infrastructure.persistence.jpa.entities.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    List<ReviewEntity> findByAdvisor_Id(Long advisorId);
    List<ReviewEntity> findByFarmer_Id(Long farmerId);
    Optional<ReviewEntity> findByAdvisor_IdAndFarmer_Id(Long advisorId, Long farmerId);
}
