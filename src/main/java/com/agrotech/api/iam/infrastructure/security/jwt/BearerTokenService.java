package com.agrotech.api.iam.infrastructure.security.jwt;

import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;

public interface BearerTokenService {
    String generateToken(AuthenticatedUser authenticatedUser);

    AuthenticatedUser getAuthenticatedUser(String token);

    boolean validateToken(String token);

    String getBearerTokenFrom(HttpServletRequest request);
}
