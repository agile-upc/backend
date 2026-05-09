package com.agrotech.api.profile.domain.model.aggregates;

import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.profile.domain.model.commands.CreateProfileCommand;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class ProfileTest {
    @Test
    public void test_create_farmer_profile_with_valid_command() {
        // Arrange
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        String firstName = "John";
        String lastName = "Doe";
        String city = "Lima";
        String country = "Perú";
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        String description = "Hola soy un granjero.";
        String photo = "john_doe.jpg";
        String occupation = "";
        Integer experience = 0;

        CreateProfileCommand command = new CreateProfileCommand(
                mockUser.getId(), firstName, lastName, city, country, birthDate, description, photo, occupation, experience
        );

        // Act
        Profile profile = new Profile(command, mockUser);

        // Assert
        assertEquals(firstName, profile.getFirstName());
        assertEquals(lastName, profile.getLastName());
        assertEquals(city, profile.getCity());
        assertEquals(country, profile.getCountry());
        assertEquals(birthDate, profile.getBirthDate());
        assertEquals(description, profile.getDescription());
        assertEquals(photo, profile.getPhoto());
        assertEquals(occupation, profile.getOccupation());
        assertEquals(experience, profile.getExperience());
        assertEquals(mockUser, profile.getUser());
    }

    @Test
    public void test_create_advisor_profile_with_valid_command() {
        // Arrange
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        String firstName = "Mario";
        String lastName = "Ramos";
        String city = "Arequipa";
        String country = "Perú";
        LocalDate birthDate = LocalDate.of(1989, 12, 14);
        String description = "Hola soy un asesor.";
        String photo = "mario_ramos.jpg";
        String occupation = "Consultor Agrícola";
        Integer experience = 9;

        CreateProfileCommand command = new CreateProfileCommand(
                mockUser.getId(), firstName, lastName, city, country, birthDate, description, photo, occupation, experience
        );

        // Act
        Profile profile = new Profile(command, mockUser);

        // Assert
        assertEquals(firstName, profile.getFirstName());
        assertEquals(lastName, profile.getLastName());
        assertEquals(city, profile.getCity());
        assertEquals(country, profile.getCountry());
        assertEquals(birthDate, profile.getBirthDate());
        assertEquals(description, profile.getDescription());
        assertEquals(photo, profile.getPhoto());
        assertEquals(occupation, profile.getOccupation());
        assertEquals(experience, profile.getExperience());
        assertEquals(mockUser, profile.getUser());
    }

}