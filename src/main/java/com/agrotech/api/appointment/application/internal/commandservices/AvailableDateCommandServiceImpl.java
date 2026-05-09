package com.agrotech.api.appointment.application.internal.commandservices;

import com.agrotech.api.appointment.application.internal.outboundservices.acl.ExternalProfilesService;
import com.agrotech.api.appointment.domain.exceptions.*;
import com.agrotech.api.appointment.domain.model.commands.CreateAvailableDateCommand;
import com.agrotech.api.appointment.domain.model.commands.DeleteAvailableDateCommand;
import com.agrotech.api.appointment.domain.model.commands.UpdateAvailableDateCommand;
import com.agrotech.api.appointment.domain.model.commands.UpdateAvailableDateStatusCommand;
import com.agrotech.api.appointment.domain.model.entities.AvailableDate;
import com.agrotech.api.appointment.domain.services.AvailableDateCommandService;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.mappers.AvailableDateMapper;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repositories.AvailableDateRepository;
import com.agrotech.api.shared.domain.exceptions.AdvisorNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AvailableDateCommandServiceImpl implements AvailableDateCommandService {
    private final AvailableDateRepository availableDateRepository;
    private final ExternalProfilesService externalProfilesService;

    public AvailableDateCommandServiceImpl(AvailableDateRepository availableDateRepository, ExternalProfilesService externalProfilesService) {
        this.availableDateRepository = availableDateRepository;
        this.externalProfilesService = externalProfilesService;
    }

    @Override
    public Long handle(CreateAvailableDateCommand command) {
        var advisor = externalProfilesService.fetchAdvisorById(command.advisorId())
                .orElseThrow(() -> new AdvisorNotFoundException(command.advisorId()));
        availableDateRepository.findByAdvisor_IdAndScheduledDateAndStartTimeAndEndTime(
                        command.advisorId(), command.scheduledDate(), command.startTime(), command.endTime())
                .ifPresent(existing -> {
                    throw new SameAvailableDateException(command.scheduledDate(), command.startTime(), command.endTime());
                });
        var availableDate = AvailableDate.create(command, advisor);
        var availableDateEntity = availableDateRepository.save(AvailableDateMapper.toEntity(availableDate));
        return availableDateEntity.getId();
    }

    @Override
    public Optional<AvailableDate> handle(UpdateAvailableDateCommand command) {
        var availableDateEntity = availableDateRepository.findById(command.id())
                .orElseThrow(() -> new AvailableDateNotFoundException(command.id()));
        AvailableDate.validateTimeFormat(command.startTime(), command.endTime());
        AvailableDate.validateTimeRange(command.startTime(), command.endTime());
        availableDateEntity.update(command);
        availableDateRepository.save(availableDateEntity);
        return Optional.of(AvailableDateMapper.toDomain(availableDateEntity));
    }

    @Override
    public void handle(DeleteAvailableDateCommand command) {
        var availableDate = availableDateRepository.findById(command.id())
                .orElseThrow(() -> new AvailableDateNotFoundException(command.id()));
        availableDateRepository.delete(availableDate);
    }

    @Override
    public void handle(UpdateAvailableDateStatusCommand command) {
        var availableDateEntity = availableDateRepository.findById(command.id())
                .orElseThrow(() -> new AvailableDateNotFoundException(command.id()));
        availableDateEntity.updateStatus(command.status());
        availableDateRepository.save(availableDateEntity);
    }
}
