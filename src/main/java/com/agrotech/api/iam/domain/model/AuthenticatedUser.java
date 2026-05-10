package com.agrotech.api.iam.domain.model;

import com.agrotech.api.iam.domain.valueobject.UserRole;

public record AuthenticatedUser(
        Long userId,
        Long profileId,
        String username,
        UserRole role,
        Long farmerId,
        Long advisorId
) {
    public void validate() {
        if (userId == null || profileId == null || role == null || username == null || username.isBlank()) {
            throw new IllegalArgumentException("Token is missing required identity claims");
        }

        boolean hasFarmer = farmerId != null;
        boolean hasAdvisor = advisorId != null;

        if (role == UserRole.FARMER && (!hasFarmer || hasAdvisor)) {
            throw new IllegalArgumentException("Farmer token must include farmerId and exclude advisorId");
        }
        if (role == UserRole.ADVISOR && (!hasAdvisor || hasFarmer)) {
            throw new IllegalArgumentException("Advisor token must include advisorId and exclude farmerId");
        }
        if (role == UserRole.ADMIN && (hasFarmer || hasAdvisor)) {
            throw new IllegalArgumentException("Admin token must not include farmerId or advisorId");
        }
    }
}
