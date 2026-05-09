package com.agrotech.api.appointment.application.internal.eventhandlers;

import com.agrotech.api.appointment.application.internal.outboundservices.acl.ExternalNotificationsService;
import com.agrotech.api.appointment.application.internal.outboundservices.acl.ExternalProfilesService;
import com.agrotech.api.appointment.domain.exceptions.AvailableDateNotFoundException;
import com.agrotech.api.appointment.domain.exceptions.ProfileNotFoundException;
import com.agrotech.api.appointment.domain.model.commands.UpdateAvailableDateStatusCommand;
import com.agrotech.api.appointment.domain.model.events.CreateNotificationByAppointmentCreated;
import com.agrotech.api.appointment.domain.model.queries.GetAvailableDateByIdQuery;
import com.agrotech.api.appointment.domain.services.AvailableDateCommandService;
import com.agrotech.api.appointment.domain.services.AvailableDateQueryService;
import com.agrotech.api.shared.domain.exceptions.FarmerNotFoundException;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service
public class AppointmentCreatedEventHandler {
    private final ExternalProfilesService externalProfilesService;
    private final ExternalNotificationsService externalNotificationsService;
    private final AvailableDateCommandService availableDateCommandService;
    private final AvailableDateQueryService availableDateQueryService;

    public AppointmentCreatedEventHandler(ExternalProfilesService externalProfileService,
                                          ExternalNotificationsService externalNotificationsService,
                                          AvailableDateCommandService availableDateCommandService,
                                          AvailableDateQueryService availableDateQueryService) {
        this.externalProfilesService = externalProfileService;
        this.externalNotificationsService = externalNotificationsService;
        this.availableDateCommandService = availableDateCommandService;
        this.availableDateQueryService = availableDateQueryService;
    }

    @EventListener
    @Transactional
    public void onAppointmentCreated(CreateNotificationByAppointmentCreated event) {
        Date date = new Date();

        var availableDate = availableDateQueryService.handle(new GetAvailableDateByIdQuery(event.getAvailableDateId()))
                .orElseThrow(() -> new AvailableDateNotFoundException(event.getAvailableDateId()));

        availableDateCommandService.handle(new UpdateAvailableDateStatusCommand(event.getAvailableDateId(),"UNAVAILABLE"));

        var farmer = externalProfilesService.fetchFarmerById(event.getFarmerId()).orElseThrow(() -> new FarmerNotFoundException(event.getFarmerId()));
        var advisor = externalProfilesService.fetchAdvisorById(availableDate.getAdvisorId()).orElseThrow(() -> new AvailableDateNotFoundException(availableDate.getAdvisorId()));
        var profileFarmer = externalProfilesService.fetchProfileByFarmerId(event.getFarmerId()).orElseThrow(ProfileNotFoundException::new);
        var profileAdvisor = externalProfilesService.fetchProfileByAdvisorId(availableDate.getAdvisorId()).orElseThrow(ProfileNotFoundException::new);

        var meetingUrl = "https://meet.jit.si/agrotechMeeting" + event.getFarmerId() + "-" + availableDate.getAdvisorId();

        externalNotificationsService.createNotification(farmer.getUserId(), "Proximo Asesoramiento",
                "Tienes un asesoramiento programado con " + profileAdvisor.getFirstName() + " " + profileAdvisor.getLastName(),
                date);
        externalNotificationsService.createNotification(advisor.getUserId(), "Proximo Asesoramiento",
                "Tienes una asesoria programada con " + profileFarmer.getFirstName() + " " + profileFarmer.getLastName(),
                date);
    }
}