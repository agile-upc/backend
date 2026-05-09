package com.agrotech.api.appointment.application.internal.eventhandlers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agrotech.api.appointment.application.internal.outboundservices.acl.ExternalNotificationsService;
import com.agrotech.api.appointment.application.internal.outboundservices.acl.ExternalProfilesService;
import com.agrotech.api.appointment.domain.model.commands.UpdateAvailableDateStatusCommand;
import com.agrotech.api.appointment.domain.model.entities.AvailableDate;
import com.agrotech.api.appointment.domain.model.events.CreateNotificationByAppointmentCancelled;
import com.agrotech.api.appointment.domain.model.queries.GetAvailableDateByIdQuery;
import com.agrotech.api.appointment.domain.services.AvailableDateCommandService;
import com.agrotech.api.appointment.domain.services.AvailableDateQueryService;
import com.agrotech.api.profile.domain.model.entities.Advisor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

@ActiveProfiles("test")
class AppointmentDeletedEventHandlerTest {

    @Mock
    private AvailableDateQueryService availableDateQueryService;

    @Mock
    private AvailableDateCommandService availableDateCommandService;

    @Mock
    private ExternalProfilesService externalProfilesService;

    @Mock
    private ExternalNotificationsService externalNotificationsService;

    @InjectMocks
    private AppointmentDeletedEventHandler appointmentDeletedEventHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Event handler successfully updates available date status to AVAILABLE
    @Test
    public void test_appointment_deleted_event_handler_updates_available_date_status() {
        // Arrange
        Long availableDateId = 1L;
        Long advisorId = 2L;
        Long userId = 3L;

        CreateNotificationByAppointmentCancelled event = new CreateNotificationByAppointmentCancelled(this, availableDateId);
        Advisor advisor = Mockito.mock(Advisor.class);

        AvailableDate availableDate = Mockito.mock(AvailableDate.class);

        when(availableDateQueryService.handle(any(GetAvailableDateByIdQuery.class))).thenReturn(Optional.of(availableDate));
        when(availableDate.getAdvisorId()).thenReturn(advisorId);
        when(availableDate.getScheduledDate()).thenReturn(LocalDate.now());
        when(availableDate.getStartTime()).thenReturn("10:00:00");
        when(externalProfilesService.fetchAdvisorById(advisorId)).thenReturn(Optional.of(advisor));
        when(advisor.getUserId()).thenReturn(userId);

        // Act
        appointmentDeletedEventHandler.onAppointmentDeleted(event);

        // Assert
        verify(availableDateCommandService).handle(new UpdateAvailableDateStatusCommand(availableDateId, "AVAILABLE"));
        verify(externalNotificationsService).createNotification(eq(userId), eq("Cita Cancelada"),
                contains("Se ha cancelado una cita programada"), any(Date.class));
    }
}