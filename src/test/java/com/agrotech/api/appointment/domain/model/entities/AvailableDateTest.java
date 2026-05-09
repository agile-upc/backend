package com.agrotech.api.appointment.domain.model.entities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.agrotech.api.appointment.domain.model.commands.CreateAvailableDateCommand;
import com.agrotech.api.appointment.domain.model.valueobjects.AvailableDateStatus;
import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.profile.domain.model.entities.Advisor;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

@ActiveProfiles("test")
class AvailableDateTest {
    // Creating a new AvailableDate with valid CreateAvailableDateCommand and Advisor
    @Test
    public void test_create_available_date_with_valid_command() {
        // Arrange
        LocalDate scheduledDate = LocalDate.now().plusDays(1);
        String startTime = "09:00";
        String endTime = "17:00";
        Long advisorId = 1L;

        CreateAvailableDateCommand command = new CreateAvailableDateCommand(
                advisorId, scheduledDate, startTime, endTime);

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Advisor advisor = new Advisor(user);

        // Act
        AvailableDate availableDate = new AvailableDate().create(command, advisor);

        // Assert
        assertNotNull(availableDate);
        assertEquals(scheduledDate, availableDate.getScheduledDate());
        assertEquals(startTime, availableDate.getStartTime());
        assertEquals(endTime, availableDate.getEndTime());
        assertEquals(advisor, availableDate.getAdvisor());
        assertEquals(AvailableDateStatus.AVAILABLE, availableDate.getStatus());
        assertEquals("AVAILABLE", availableDate.getAvailableDateStatus());
    }
}