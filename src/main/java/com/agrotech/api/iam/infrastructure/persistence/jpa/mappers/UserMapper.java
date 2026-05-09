package com.agrotech.api.iam.infrastructure.persistence.jpa.mappers;

import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.infrastructure.persistence.jpa.entities.UserEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {
    public static User toDomain(UserEntity entity) {
        if (entity == null) return null;
        User user = new User(entity.getUsername(), entity.getPassword());
        user.setId(entity.getId());
        Set<Role> domainRoles = entity.getRoles().stream()
                .map(RoleMapper::toDomain)
                .collect(Collectors.toSet());
        user.addRoles(domainRoles.stream().toList());
        return user;
    }

    public static UserEntity toEntity(User domain) {
        if (domain == null) return null;

        return UserEntity.builder()
                .id(domain.getId())
                .username(domain.getUsername())
                .password(domain.getPassword())
                .roles(domain.getRoles().stream().map(RoleMapper::toEntity).collect(Collectors.toSet()))
                .build();
    }
}

