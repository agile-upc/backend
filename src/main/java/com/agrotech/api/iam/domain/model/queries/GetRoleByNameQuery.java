package com.agrotech.api.iam.domain.model.queries;

import com.agrotech.api.iam.domain.model.valueobjects.Roles;

public record GetRoleByNameQuery(Roles name) {
}