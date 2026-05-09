package com.agrotech.api.iam.domain.model.entities;

import com.agrotech.api.iam.domain.exceptions.InvalidRoleException;
import com.agrotech.api.iam.domain.model.valueobjects.Roles;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
class RoleTest {

    @Test
    public void test_valid_role_name_conversion() {
        // Given
        String roleName = "ROLE_ADMIN";

        // When
        Role role = Role.toRoleFromName(roleName);

        // Then
        assertNotNull(role);
        assertEquals(Roles.ROLE_ADMIN, role.getName());
    }

    @Test
    public void test_null_role_name_throws_exception() {
        // Given
        String roleName = null;

        // When & Then
        assertThrows(InvalidRoleException.class, () -> {
            Role.toRoleFromName(roleName);
        });
    }
    @Test
    public void test_create_role_with_valid_enum_value() {
        // Arrange
        Roles validRole = Roles.ROLE_ADMIN;

        // Act
        Role role = new Role(validRole);

        // Assert
        assertEquals(validRole, role.getName());
        assertEquals("ROLE_ADMIN", role.getStringName());
    }
}