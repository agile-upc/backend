package com.agrotech.api.iam.application.mapper;

import com.agrotech.api.iam.infrastructure.web.dto.AuthenticatedUserResource;
import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import com.agrotech.api.iam.domain.model.AuthenticationResult;
import com.agrotech.api.iam.domain.model.User;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.domain.model.Farmer;
import com.agrotech.api.profile.domain.model.Profile;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
    public AuthenticatedUser toAuthenticatedUser(User user, Profile profile, Farmer farmer, Advisor advisor) {
        return new AuthenticatedUser(
                user.getId(),
                profile.getId(),
                user.getUsername(),
                user.getRole(),
                farmer != null ? farmer.getId() : null,
                advisor != null ? advisor.getId() : null
        );
    }

    public AuthenticatedUserResource toAuthenticatedUserResource(AuthenticationResult authenticationResult) {
        AuthenticatedUser authenticatedUser = authenticationResult.authenticatedUser();
        return new AuthenticatedUserResource(
                authenticatedUser.userId(),
                authenticatedUser.profileId(),
                authenticatedUser.username(),
                authenticatedUser.role().name(),
                authenticatedUser.farmerId(),
                authenticatedUser.advisorId(),
                authenticationResult.token()
        );
    }
}
