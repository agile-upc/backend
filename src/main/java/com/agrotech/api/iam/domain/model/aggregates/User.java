package com.agrotech.api.iam.domain.model.aggregates;

import com.agrotech.api.iam.domain.model.entities.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class User {
    private Long id;
    private String username;
    private String password;
    private Set<Role> roles;

    public User() {
        this.roles = new HashSet<>();
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>();
    }

    public User(String username, String password, List<Role> roles) {
        this(username, password);
        addRoles(roles);
    }

    public User addRole(Role role) {
        this.roles.add(role);
        return this;
    }

    public User addRoles(List<Role> roles) {
        var validatedRoleSet = Role.validateRoleSet(roles);
        this.roles.addAll(validatedRoleSet);
        return this;
    }
}
