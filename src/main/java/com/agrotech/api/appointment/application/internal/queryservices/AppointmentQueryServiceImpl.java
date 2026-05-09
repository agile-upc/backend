package com.agrotech.api.appointment.application.internal.queryservices;

import com.agrotech.api.appointment.domain.model.aggregates.Appointment;
import com.agrotech.api.appointment.domain.model.queries.*;
import com.agrotech.api.appointment.domain.services.AppointmentQueryService;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.mappers.AppointmentMapper;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repositories.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentQueryServiceImpl implements AppointmentQueryService {
    private final AppointmentRepository appointmentRepository;

    public AppointmentQueryServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public List<Appointment> handle(GetAllAppointmentsQuery query) {
        return this.appointmentRepository.findAll()
                .stream()
                .map(AppointmentMapper::toDomain)
                .map(this::updateStatusIfNeeded)
                .toList();
    }

    @Override
    public Optional<Appointment> handle(GetAppointmentByIdQuery query) {
        return this.appointmentRepository.findById(query.id())
                .map(AppointmentMapper::toDomain)
                .map(this::updateStatusIfNeeded);
    }

    @Override
    public List<Appointment> handle(GetAppointmentsByFarmerIdQuery query) {
        return this.appointmentRepository.findByFarmer_Id(query.farmerId())
                .stream()
                .map(AppointmentMapper::toDomain)
                .map(this::updateStatusIfNeeded)
                .toList();
    }

    @Override
    public List<Appointment> handle(GetAppointmentsByAdvisorIdQuery query) {
        return this.appointmentRepository.findByAvailableDate_Advisor_Id(query.advisorId())
                .stream()
                .map(AppointmentMapper::toDomain)
                .map(this::updateStatusIfNeeded)
                .toList();
    }

    @Override
    public List<Appointment> handle(GetAppointmentsByAdvisorIdAndFarmerIdQuery query) {
        return this.appointmentRepository.findByAvailableDate_Advisor_IdAndFarmer_Id(query.advisorId(), query.farmerId())
                .stream()
                .map(AppointmentMapper::toDomain)
                .map(this::updateStatusIfNeeded)
                .toList();
    }

    private Appointment updateStatusIfNeeded(Appointment appointment) {
        var oldStatus = appointment.getStatus();
        appointment.updateStatusBasedOnTime(LocalDateTime.now());
        if (!appointment.getStatus().equals(oldStatus)) {
            appointmentRepository.save(AppointmentMapper.toEntity(appointment));
        }
        return appointment;
    }
}