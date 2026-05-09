package com.agrotech.api.appointment.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.agrotech.api.appointment.application.internal.outboundservices.acl.ExternalProfilesService;
import com.agrotech.api.appointment.domain.exceptions.AvailableDateNotFoundException;
import com.agrotech.api.appointment.domain.model.commands.CreateAppointmentCommand;
import com.agrotech.api.appointment.domain.model.entities.AvailableDate;
import com.agrotech.api.appointment.domain.model.events.CreateNotificationByAppointmentCreated;
import com.agrotech.api.appointment.domain.model.queries.GetAvailableDateByIdQuery;
import com.agrotech.api.appointment.domain.model.valueobjects.AppointmentStatus;
import com.agrotech.api.appointment.domain.model.valueobjects.AvailableDateStatus;
import com.agrotech.api.appointment.domain.services.AvailableDateQueryService;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.entities.AppointmentEntity;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repositories.AppointmentRepository;
import com.agrotech.api.profile.domain.model.entities.Advisor;
import com.agrotech.api.profile.domain.model.entities.Farmer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ActiveProfiles("test")
class AppointmentCommandServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private ExternalProfilesService externalProfilesService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private AvailableDateQueryService availableDateQueryService;

    @InjectMocks
    private AppointmentCommandServiceImpl appointmentCommandService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_handle_create_appointment_command_success() {
        // Arrange
        Long availableDateId = 1L;
        Long farmerId = 2L;
        Long advisorId = 3L;
        String message = "Appointment request";
        Long appointmentId = 10L;
        String expectedMeetingUrl = "https://meet.jit.si/agrotechMeeting" + farmerId + "-" + advisorId;

        CreateAppointmentCommand command = new CreateAppointmentCommand(availableDateId, farmerId, message);

        AvailableDate availableDate = mock(AvailableDate.class);
        when(availableDate.getStatus()).thenReturn(AvailableDateStatus.AVAILABLE);
        when(availableDate.getAdvisorId()).thenReturn(advisorId);
        when(availableDate.getId()).thenReturn(availableDateId);

        Advisor advisor = mock(Advisor.class);
        when(advisor.getId()).thenReturn(advisorId);

        Farmer farmer = mock(Farmer.class);
        when(farmer.getId()).thenReturn(farmerId);

        when(availableDateQueryService.handle(
                argThat((GetAvailableDateByIdQuery query) -> query.id().equals(availableDateId))
        )).thenReturn(Optional.of(availableDate));

        when(externalProfilesService.fetchAdvisorById(advisorId)).thenReturn(Optional.of(advisor));
        when(externalProfilesService.fetchFarmerById(farmerId)).thenReturn(Optional.of(farmer));

        ArgumentCaptor<AppointmentEntity> appointmentCaptor = ArgumentCaptor.forClass(AppointmentEntity.class);
        when(appointmentRepository.save(appointmentCaptor.capture())).thenAnswer(invocation -> {
            AppointmentEntity savedEntity = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedEntity, "id", appointmentId);
            return savedEntity;
        });

        // Act
        Long result = appointmentCommandService.handle(command);

        // Assert
        assertEquals(appointmentId, result);

        AppointmentEntity capturedAppointmentEntity = appointmentCaptor.getValue();
        assertEquals(message, capturedAppointmentEntity.getMessage());
        assertEquals(AppointmentStatus.PENDING, capturedAppointmentEntity.getStatus());
        assertEquals(farmerId, capturedAppointmentEntity.getFarmer().getId());
        assertEquals(availableDateId, capturedAppointmentEntity.getAvailableDate().getId());
        assertEquals(expectedMeetingUrl, capturedAppointmentEntity.getMeetingUrl());

        verify(appointmentRepository).save(any(AppointmentEntity.class));
        verify(eventPublisher).publishEvent(argThat(event ->
                event instanceof CreateNotificationByAppointmentCreated &&
                        ((CreateNotificationByAppointmentCreated) event).getFarmerId().equals(farmerId) &&
                        ((CreateNotificationByAppointmentCreated) event).getAvailableDateId().equals(availableDateId)
        ));
    }

    @Test
    public void test_handle_create_appointment_command_with_nonexistent_available_date() {
        // Arrange
        Long availableDateId = 1L;
        Long farmerId = 2L;
        String message = "Appointment request";

        CreateAppointmentCommand command = new CreateAppointmentCommand(availableDateId, farmerId, message);

        when(availableDateQueryService.handle(any(GetAvailableDateByIdQuery.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AvailableDateNotFoundException.class, () -> {
            appointmentCommandService.handle(command);
        });

        verify(availableDateQueryService).handle(any(GetAvailableDateByIdQuery.class));
        verifyNoInteractions(appointmentRepository);
        verifyNoInteractions(eventPublisher);
    }
}
