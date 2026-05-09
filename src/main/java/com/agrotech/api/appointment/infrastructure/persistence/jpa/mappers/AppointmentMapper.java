package com.agrotech.api.appointment.infrastructure.persistence.jpa.mappers;

import com.agrotech.api.appointment.domain.model.aggregates.Appointment;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.entities.AppointmentEntity;
import com.agrotech.api.profile.infrastructure.persistence.jpa.mappers.FarmerMapper;

public class AppointmentMapper {
    public static Appointment toDomain(AppointmentEntity entity) {
        if (entity == null) return null;
        return new Appointment(
                entity.getId(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getMeetingUrl(),
                FarmerMapper.toDomain(entity.getFarmer()),
                AvailableDateMapper.toDomain(entity.getAvailableDate())
        );
    }

    public static AppointmentEntity toEntity(Appointment domain) {
        if (domain == null) return null;
        return AppointmentEntity.builder()
                .id(domain.getId())
                .message(domain.getMessage())
                .status(domain.getStatus())
                .meetingUrl(domain.getMeetingUrl())
                .farmer(FarmerMapper.toEntity(domain.getFarmer()))
                .availableDate(AvailableDateMapper.toEntity(domain.getAvailableDate()))
                .build();
    }
}