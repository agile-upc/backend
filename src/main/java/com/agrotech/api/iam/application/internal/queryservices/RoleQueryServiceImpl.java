package com.agrotech.api.iam.application.internal.queryservices;

import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.domain.model.queries.GetAllRolesQuery;
import com.agrotech.api.iam.domain.model.queries.GetRoleByNameQuery;
import com.agrotech.api.iam.domain.services.RoleQueryService;
import com.agrotech.api.iam.infrastructure.persistence.jpa.mappers.RoleMapper;
import com.agrotech.api.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleQueryServiceImpl implements RoleQueryService {
    private final RoleRepository roleRepository;

    public RoleQueryServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> handle(GetAllRolesQuery query) {
        return roleRepository.findAll()
                .stream()
                .map(RoleMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Role> handle(GetRoleByNameQuery query) {
        return roleRepository.findByName(query.name())
                .map(RoleMapper::toDomain);
    }
}