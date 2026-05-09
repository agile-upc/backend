package com.agrotech.api.appointment.infrastructure.persistence.jpa.mappers;

import com.agrotech.api.appointment.domain.model.entities.AvailableDate;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.entities.AvailableDateEntity;
import com.agrotech.api.profile.infrastructure.persistence.jpa.mappers.AdvisorMapper;

public class AvailableDateMapper {
    public static AvailableDate toDomain(AvailableDateEntity entity) {
        if (entity == null) return null;
        return new AvailableDate(
                entity.getId(),
                entity.getScheduledDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus(),
                AdvisorMapper.toDomain(entity.getAdvisor())
        );
    }

    public static AvailableDateEntity toEntity(AvailableDate domain) {
        if (domain == null) return null;
        return AvailableDateEntity.builder()
                .id(domain.getId())
                .scheduledDate(domain.getScheduledDate())
                .startTime(domain.getStartTime())
                .endTime(domain.getEndTime())
                .status(domain.getStatus())
                .advisor(AdvisorMapper.toEntity(domain.getAdvisor()))
                .build();
    }
}