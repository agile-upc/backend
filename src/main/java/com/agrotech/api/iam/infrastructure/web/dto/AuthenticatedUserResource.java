package com.agrotech.api.iam.infrastructure.web.dto;

public record AuthenticatedUserResource(
        Long userId,
        Long profileId,
        String username,
        String role,
        Long farmerId,
        Long advisorId,
        String token
) {
}
