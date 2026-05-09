package com.agrotech.api.appointment.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.agrotech.api.appointment.domain.model.commands.CreateAppointmentCommand;
import com.agrotech.api.appointment.domain.model.entities.AvailableDate;
import com.agrotech.api.appointment.domain.model.valueobjects.AppointmentStatus;
import com.agrotech.api.profile.domain.model.entities.Farmer;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class AppointmentTest {
    // Creating a new Appointment with valid CreateAppointmentCommand sets correct initial values
    @Test
    public void test_create_appointment_with_valid_command_sets_correct_values() {
        // Arrange
        Long availableDateId = 1L;
        Long farmerId = 2L;
        String message = "I would like to schedule an appointment";

        CreateAppointmentCommand command = new CreateAppointmentCommand(availableDateId, farmerId, message);

        Farmer farmer = mock(Farmer.class);
        when(farmer.getId()).thenReturn(farmerId);

        AvailableDate availableDate = mock(AvailableDate.class);
        when(availableDate.getId()).thenReturn(availableDateId);

        // Act
        Appointment appointment = Appointment.create(command, farmer, availableDate);

        // Assert
        assertEquals(message, appointment.getMessage());
        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());
        assertEquals(farmer, appointment.getFarmer());
        assertEquals(availableDate, appointment.getAvailableDate());
        assertNotNull(appointment.getMeetingUrl());
        assertEquals(farmerId, appointment.getFarmerId());
        assertEquals(availableDateId, appointment.getAvailableDateId());
        assertEquals("PENDING", appointment.getAppointmentStatus());
    }
}