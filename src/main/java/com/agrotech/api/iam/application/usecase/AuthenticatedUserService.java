package com.agrotech.api.iam.application.usecase;

import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticatedUserService {
    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user");
        }
        return authenticatedUser;
    }

    public AuthenticatedUser requireRole(UserRole role) {
        AuthenticatedUser authenticatedUser = getCurrentUser();
        if (authenticatedUser.role() != role) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operation requires role " + role.name());
        }
        return authenticatedUser;
    }
}
