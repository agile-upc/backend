package com.agrotech.api.iam.domain.services;

import com.agrotech.api.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
    void handle(SeedRolesCommand command);
}