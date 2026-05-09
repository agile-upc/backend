package com.agrotech.api.appointment.application.internal.commandservices;

import com.agrotech.api.appointment.application.internal.outboundservices.acl.ExternalProfilesService;
import com.agrotech.api.appointment.domain.exceptions.*;
import com.agrotech.api.appointment.domain.model.events.CreateNotificationByAppointmentCancelled;
import com.agrotech.api.appointment.domain.model.events.CreateNotificationByAppointmentCreated;
import com.agrotech.api.appointment.domain.model.queries.GetAvailableDateByIdQuery;
import com.agrotech.api.appointment.domain.services.AvailableDateQueryService;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.mappers.AppointmentMapper;
import com.agrotech.api.shared.domain.exceptions.*;
import com.agrotech.api.appointment.domain.model.aggregates.Appointment;
import com.agrotech.api.appointment.domain.model.commands.CreateAppointmentCommand;
import com.agrotech.api.appointment.domain.model.commands.DeleteAppointmentCommand;
import com.agrotech.api.appointment.domain.model.commands.UpdateAppointmentCommand;
import com.agrotech.api.appointment.domain.services.AppointmentCommandService;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repositories.AppointmentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AppointmentCommandServiceImpl implements AppointmentCommandService {
    private final AppointmentRepository appointmentRepository;
    private final ExternalProfilesService externalProfilesService;
    private final ApplicationEventPublisher eventPublisher;
    private final AvailableDateQueryService availableDateQueryService;

    public AppointmentCommandServiceImpl(AppointmentRepository appointmentRepository,
                                         ExternalProfilesService externalProfilesService,
                                         ApplicationEventPublisher eventPublisher,
                                         AvailableDateQueryService availableDateQueryService) {
        this.appointmentRepository = appointmentRepository;
        this.externalProfilesService = externalProfilesService;
        this.eventPublisher = eventPublisher;
        this.availableDateQueryService = availableDateQueryService;
    }

    @Override
    @Transactional
    public Long handle(CreateAppointmentCommand command) {
        var availableDate = availableDateQueryService.handle(new GetAvailableDateByIdQuery(command.availableDateId()))
                .orElseThrow(() -> new AvailableDateNotFoundException(command.availableDateId()));
        var farmer = externalProfilesService.fetchFarmerById(command.farmerId())
                .orElseThrow(() -> new FarmerNotFoundException(command.farmerId()));
        var appointment = Appointment.create(command, farmer, availableDate);
        var appointmentEntity = appointmentRepository.save(AppointmentMapper.toEntity(appointment));
        eventPublisher.publishEvent(new CreateNotificationByAppointmentCreated(this, command.farmerId(), command.availableDateId()));
        return appointmentEntity.getId();
    }

    @Override
    public Optional<Appointment> handle(UpdateAppointmentCommand command) {
        var appointmentEntity = appointmentRepository.findById(command.id())
                .orElseThrow(() -> new AppointmentNotFoundException(command.id()));
        if (command.status() != null) Appointment.validateStatus(command.status());
        appointmentEntity.update(command);
        appointmentRepository.save(appointmentEntity);
        return Optional.of(AppointmentMapper.toDomain(appointmentEntity));
    }

    @Override
    @Transactional
    public void handle(DeleteAppointmentCommand command) {
        var appointmentEntity = appointmentRepository.findById(command.id())
                .orElseThrow(() -> new AppointmentNotFoundException(command.id()));
        var availableDate = availableDateQueryService.handle(new GetAvailableDateByIdQuery(appointmentEntity.getAvailableDate().getId()))
                .orElseThrow(() -> new AvailableDateNotFoundException(appointmentEntity.getAvailableDate().getId()));
        eventPublisher.publishEvent(new CreateNotificationByAppointmentCancelled(this, availableDate.getId()));
        appointmentRepository.delete(appointmentEntity);
    }
}
