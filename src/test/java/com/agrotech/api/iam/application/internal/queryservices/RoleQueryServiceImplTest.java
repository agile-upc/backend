package com.agrotech.api.iam.application.internal.queryservices;

import static org.junit.jupiter.api.Assertions.*;

import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.model.queries.GetAllRolesQuery;
import com.agrotech.api.iam.domain.model.valueobjects.Roles;
import com.agrotech.api.iam.infrastructure.persistence.jpa.entities.RoleEntity;
import com.agrotech.api.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("test")
class RoleQueryServiceImplTest {

    // Test: handle(GetAllRolesQuery) returns all roles from repository
    @Test
    public void test_handle_get_all_roles_query_returns_all_roles() {
        // Arrange
        RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
        List<RoleEntity> roleEntities = List.of(
                new RoleEntity(1L, Roles.ROLE_ADMIN),
                new RoleEntity(2L, Roles.ROLE_USER)
        );
        Mockito.when(roleRepository.findAll()).thenReturn(roleEntities);

        RoleQueryServiceImpl roleQueryService = new RoleQueryServiceImpl(roleRepository);
        GetAllRolesQuery query = new GetAllRolesQuery();

        // Act
        List<Role> actualRoles = roleQueryService.handle(query);

        // Assert
        assertEquals(2, actualRoles.size());
        assertEquals(1L, actualRoles.get(0).getId());
        assertEquals(Roles.ROLE_ADMIN, actualRoles.get(0).getName());
        assertEquals(2L, actualRoles.get(1).getId());
        assertEquals(Roles.ROLE_USER, actualRoles.get(1).getName());
        Mockito.verify(roleRepository).findAll();
    }

    // Test: handle(GetAllRolesQuery) with null query parameter
    @Test
    public void test_handle_get_all_roles_query_with_null_parameter() {
        // Arrange
        RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
        List<RoleEntity> roleEntities = List.of(
                new RoleEntity(2L, Roles.ROLE_USER)
        );
        Mockito.when(roleRepository.findAll()).thenReturn(roleEntities);

        RoleQueryServiceImpl roleQueryService = new RoleQueryServiceImpl(roleRepository);

        // Act
        List<Role> actualRoles = roleQueryService.handle((GetAllRolesQuery) null);

        // Assert
        assertEquals(1, actualRoles.size());
        assertEquals(2L, actualRoles.getFirst().getId());
        assertEquals(Roles.ROLE_USER, actualRoles.getFirst().getName());
        Mockito.verify(roleRepository).findAll();
    }
}
