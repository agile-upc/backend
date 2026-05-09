package com.agrotech.api.iam.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.*;

import com.agrotech.api.iam.infrastructure.persistence.jpa.entities.UserEntity;
import com.agrotech.api.iam.infrastructure.persistence.jpa.mappers.UserMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

@ActiveProfiles("test")
class UserTest {
    // Creating a User with username and password initializes empty roles collection
    @Test
    public void test_user_constructor_initializes_empty_collections() {
        // Arrange
        String username = "test@example.com";
        String password = "password123";

        // Act
        User user = new User(username, password);

        // Assert
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
    }
}