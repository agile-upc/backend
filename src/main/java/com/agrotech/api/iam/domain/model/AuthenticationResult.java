package com.agrotech.api.iam.domain.model;

public record AuthenticationResult(
        AuthenticatedUser authenticatedUser,
        String token,
        String refreshToken
) {
}
