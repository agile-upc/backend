package com.agrotech.api.iam.interfaces.rest.transform;


import com.agrotech.api.iam.domain.model.aggregates.User;
import com.agrotech.api.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        return new AuthenticatedUserResource(user.getId(), user.getUsername(), token);
    }
}