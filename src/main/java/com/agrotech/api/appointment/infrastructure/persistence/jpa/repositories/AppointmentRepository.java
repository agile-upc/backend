package com.agrotech.api.appointment.infrastructure.persistence.jpa.repositories;

import com.agrotech.api.appointment.infrastructure.persistence.jpa.entities.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    List<AppointmentEntity> findByAvailableDate_Advisor_Id(Long advisorId);
    List<AppointmentEntity> findByFarmer_Id(Long farmerId);
    List<AppointmentEntity> findByAvailableDate_Advisor_IdAndFarmer_Id(Long advisorId, Long farmerId);
}
