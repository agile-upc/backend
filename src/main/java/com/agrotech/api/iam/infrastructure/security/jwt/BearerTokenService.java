package com.agrotech.api.iam.infrastructure.security.jwt;

import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;

public interface BearerTokenService {
    String generateAccessToken(AuthenticatedUser authenticatedUser);

    String generateRefreshToken(AuthenticatedUser authenticatedUser);

    AuthenticatedUser getAuthenticatedUser(String token);

    AuthenticatedUser getAuthenticatedUserFromRefreshToken(String token);

    boolean validateToken(String token);

    boolean validateRefreshToken(String token);

    String getBearerTokenFrom(HttpServletRequest request);
}
