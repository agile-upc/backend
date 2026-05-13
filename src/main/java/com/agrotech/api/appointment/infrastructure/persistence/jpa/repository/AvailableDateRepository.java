package com.agrotech.api.appointment.infrastructure.persistence.jpa.repository;

import com.agrotech.api.appointment.domain.valueobject.AvailableDateStatus;
import com.agrotech.api.appointment.domain.model.AvailableDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AvailableDateRepository extends JpaRepository<AvailableDate, Long> {
    List<AvailableDate> findByAdvisor_Id(Long advisorId);
    List<AvailableDate> findByAdvisor_IdAndStatus(Long advisor_id, AvailableDateStatus status);
    List<AvailableDate> findByStatus(AvailableDateStatus availableDateStatus);
    List<AvailableDate> findByStatusAndScheduledDateGreaterThanEqualOrderByScheduledDateAscStartTimeAsc(
            AvailableDateStatus status,
            LocalDate scheduledDate
    );
    Optional<AvailableDate> findByAdvisor_IdAndScheduledDateAndStartTimeAndEndTime(Long advisorId, LocalDate scheduledDate, String startTime, String endTime
    );
}
