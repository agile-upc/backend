package com.agrotech.api.iam.application.internal.commandservices;

import com.agrotech.api.iam.domain.model.commands.SeedRolesCommand;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.model.valueobjects.Roles;
import com.agrotech.api.iam.domain.services.RoleCommandService;
import com.agrotech.api.iam.infrastructure.persistence.jpa.entities.RoleEntity;
import com.agrotech.api.iam.infrastructure.persistence.jpa.mappers.RoleMapper;
import com.agrotech.api.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class RoleCommandServiceImpl implements RoleCommandService {
    private final RoleRepository roleRepository;

    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void handle(SeedRolesCommand command) {
        Arrays.stream(Roles.values()).forEach(role -> {
            if(!roleRepository.existsByName(role)) {
                roleRepository.save(RoleMapper.toEntity(new Role(Roles.valueOf(role.name()))));
            }
        });
    }
}