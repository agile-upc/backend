package com.agrotech.api.appointment.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.agrotech.api.appointment.application.internal.outboundservices.acl.ExternalProfilesService;
import com.agrotech.api.appointment.domain.model.commands.CreateAvailableDateCommand;
import com.agrotech.api.appointment.domain.model.entities.AvailableDate;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.entities.AvailableDateEntity;
import com.agrotech.api.appointment.infrastructure.persistence.jpa.repositories.AvailableDateRepository;
import com.agrotech.api.shared.domain.exceptions.AdvisorNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

@ActiveProfiles("test")
class AvailableDateCommandServiceImplTest {
    @Mock
    private AvailableDateRepository availableDateRepository;

    @Mock
    private ExternalProfilesService externalProfilesService;

    @InjectMocks
    private AvailableDateCommandServiceImpl availableDateCommandService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    // Successfully create a new available date with valid inputs
    @Test
    public void test_handle_create_available_date_with_advisor_not_found() {
        Long advisorId = 1L;
        LocalDate scheduledDate = LocalDate.now().plusDays(1);
        String startTime = "10:00";
        String endTime = "11:00";

        CreateAvailableDateCommand command = new CreateAvailableDateCommand(advisorId, scheduledDate, startTime, endTime);

        when(externalProfilesService.fetchAdvisorById(advisorId)).thenReturn(Optional.empty());

        AdvisorNotFoundException exception = assertThrows(AdvisorNotFoundException.class, () -> {
            availableDateCommandService.handle(command);
        });

        assertEquals("Advisor with id " + advisorId + " not found", exception.getMessage());
        verify(externalProfilesService).fetchAdvisorById(advisorId);
        verify(availableDateRepository, never()).save(any(AvailableDateEntity.class));
    }
}