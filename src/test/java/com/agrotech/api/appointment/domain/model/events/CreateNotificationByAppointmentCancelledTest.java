package com.agrotech.api.appointment.domain.model.events;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class CreateNotificationByAppointmentCancelledTest {
    // Create event with valid source and availableDateId
    @Test
    public void test_create_event_with_valid_source_and_available_date_id() {
        // Arrange
        Object source = new Object();
        Long availableDateId = 123L;

        // Act
        CreateNotificationByAppointmentCancelled event = new CreateNotificationByAppointmentCancelled(source, availableDateId);

        // Assert
        assertEquals(source, event.getSource());
        assertEquals(availableDateId, event.getAvailableDateId());
    }
}