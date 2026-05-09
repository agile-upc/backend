package com.agrotech.api.iam.interfaces.rest.transform;

import com.agrotech.api.iam.domain.model.entities.Role;
import com.agrotech.api.iam.interfaces.rest.resources.RoleResource;

public class RoleResourceFromEntityAssembler {
    public static RoleResource toResourceFromEntity(Role role) {
        return new RoleResource(role.getId(), role.getStringName());
    }
}