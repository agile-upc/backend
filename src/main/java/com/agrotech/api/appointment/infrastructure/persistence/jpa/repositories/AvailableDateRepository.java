package com.agrotech.api.appointment.infrastructure.persistence.jpa.repositories;

import com.agrotech.api.appointment.domain.model.valueobjects.AvailableDateStatus;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.entities.AvailableDateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AvailableDateRepository extends JpaRepository<AvailableDateEntity, Long> {
    List<AvailableDateEntity> findByAdvisor_Id(Long advisorId);
    List<AvailableDateEntity> findByAdvisor_IdAndStatus(Long advisor_id, AvailableDateStatus status);
    List<AvailableDateEntity> findByStatus(AvailableDateStatus availableDateStatus);
    Optional<AvailableDateEntity> findByAdvisor_IdAndScheduledDateAndStartTimeAndEndTime(Long advisorId, LocalDate scheduledDate, String startTime, String endTime
    );
}
