package com.agrotech.api.iam.domain.model.entities;

import com.agrotech.api.iam.domain.exceptions.InvalidRoleException;
import com.agrotech.api.iam.domain.model.valueobjects.Roles;

import java.util.List;

public class Role {
    private Long id;
    private Roles name;

    public Role(Roles name) {
        this.name = name;
    }

    public Role(Long id, Roles name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public Roles getName() {
        return name;
    }

    public String getStringName() {
        return name.name();
    }

    public static Role getDefaultRole() {
        return new Role(Roles.ROLE_USER);
    }

    public static Role toRoleFromName(String name) {
        try {
            return new Role(Roles.valueOf(name));
        } catch (RuntimeException e) {
            throw new InvalidRoleException(name);
        }
    }

    public static List<Role> validateRoleSet(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of(getDefaultRole());
        }
        return roles;
    }
}