package com.agrotech.api.management.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.management.domain.model.commands.CreateEnclosureCommand;
import com.agrotech.api.profile.domain.model.entities.Farmer;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class EnclosureTest {
    // Creating a new Enclosure with valid CreateEnclosureCommand and Farmer
    @Test
    public void test_create_enclosure_with_valid_command() {
        // Arrange
        String name = "Cattle Pen";
        Integer capacity = 50;
        String type = "Outdoor";
        Long farmerId = 1L;

        CreateEnclosureCommand command = new CreateEnclosureCommand(name, capacity, type, farmerId);

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(farmerId);

        Farmer farmer = mock(Farmer.class);
        when(farmer.getId()).thenReturn(farmerId);
        when(farmer.getUser()).thenReturn(mockUser);

        // Act
        Enclosure enclosure = new Enclosure(command, farmer);

        // Assert
        assertEquals(name, enclosure.getName());
        assertEquals(capacity, enclosure.getCapacity());
        assertEquals(type, enclosure.getType());
        assertEquals(farmer, enclosure.getFarmer());
        assertEquals(farmerId, enclosure.getFarmerId());
    }

    // Creating an Enclosure with null values in CreateEnclosureCommand
    @Test
    public void test_create_enclosure_with_null_values() {
        // Arrange
        String name = null;
        Integer capacity = null;
        String type = null;
        Long farmerId = 1L;

        CreateEnclosureCommand command = new CreateEnclosureCommand(name, capacity, type, farmerId);

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(farmerId);

        Farmer farmer = mock(Farmer.class);
        when(farmer.getId()).thenReturn(farmerId);
        when(farmer.getUser()).thenReturn(mockUser);

        // Act
        Enclosure enclosure = new Enclosure(command, farmer);

        // Assert
        assertNull(enclosure.getName());
        assertNull(enclosure.getCapacity());
        assertNull(enclosure.getType());
        assertEquals(farmer, enclosure.getFarmer());
        assertEquals(farmerId, enclosure.getFarmerId());
    }
}