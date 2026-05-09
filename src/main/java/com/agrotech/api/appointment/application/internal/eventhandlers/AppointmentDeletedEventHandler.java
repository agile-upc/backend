package com.agrotech.api.appointment.application.internal.eventhandlers;

import com.agrotech.api.appointment.application.internal.outboundservices.acl.ExternalNotificationsService;
import com.agrotech.api.appointment.application.internal.outboundservices.acl.ExternalProfilesService;
import com.agrotech.api.appointment.domain.exceptions.AvailableDateNotFoundException;
import com.agrotech.api.appointment.domain.model.commands.UpdateAvailableDateStatusCommand;
import com.agrotech.api.appointment.domain.model.events.CreateNotificationByAppointmentCancelled;
import com.agrotech.api.appointment.domain.model.queries.GetAvailableDateByIdQuery;
import com.agrotech.api.appointment.domain.services.AvailableDateCommandService;
import com.agrotech.api.appointment.domain.services.AvailableDateQueryService;
import com.agrotech.api.shared.domain.exceptions.AdvisorNotFoundException;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AppointmentDeletedEventHandler {
    private final ExternalNotificationsService externalNotificationsService;
    private final ExternalProfilesService externalProfilesService;
    private final AvailableDateCommandService availableDateCommandService;
    private final AvailableDateQueryService availableDateQueryService;

    public AppointmentDeletedEventHandler(ExternalNotificationsService externalNotificationsService,
                                          ExternalProfilesService externalProfilesService,
                                          AvailableDateCommandService availableDateCommandService,
                                          AvailableDateQueryService availableDateQueryService) {
        this.externalNotificationsService = externalNotificationsService;
        this.externalProfilesService = externalProfilesService;
        this.availableDateCommandService = availableDateCommandService;
        this.availableDateQueryService = availableDateQueryService;
    }

    @EventListener
    @Transactional
    public void onAppointmentDeleted(CreateNotificationByAppointmentCancelled event) {
        var availableDate = availableDateQueryService.handle(new GetAvailableDateByIdQuery(event.getAvailableDateId()))
                .orElseThrow(() -> new AvailableDateNotFoundException(event.getAvailableDateId()));

        availableDateCommandService.handle(new UpdateAvailableDateStatusCommand(event.getAvailableDateId(), "AVAILABLE"));

        var advisor = externalProfilesService.fetchAdvisorById(availableDate.getAdvisorId()).orElseThrow(
                () -> new AdvisorNotFoundException(availableDate.getAdvisorId())
        );

        externalNotificationsService.createNotification(advisor.getUserId(), "Cita Cancelada",
                "Se ha cancelado una cita programada con un agricultor para el dia " + availableDate.getScheduledDate() + " a las " + availableDate.getStartTime(),
                new Date());
    }
}
