package com.agrotech.api.iam.application.usecase;

import com.agrotech.api.iam.infrastructure.security.hashing.BCryptHashingService;
import com.agrotech.api.iam.infrastructure.web.dto.SignInResource;
import com.agrotech.api.iam.infrastructure.web.dto.SignUpResource;
import com.agrotech.api.iam.application.mapper.AuthMapper;
import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import com.agrotech.api.iam.domain.model.AuthenticationResult;
import com.agrotech.api.iam.domain.model.User;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.iam.infrastructure.persistence.jpa.repository.UserRepository;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.domain.model.Farmer;
import com.agrotech.api.profile.domain.model.Profile;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repository.AdvisorRepository;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repository.FarmerRepository;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repository.ProfileRepository;
import com.agrotech.api.iam.infrastructure.security.jwt.BearerTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final FarmerRepository farmerRepository;
    private final AdvisorRepository advisorRepository;
    private final BCryptHashingService hashingService;
    private final BearerTokenService bearerTokenService;
    private final AuthMapper authMapper;

    public AuthService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            FarmerRepository farmerRepository,
            AdvisorRepository advisorRepository,
            BCryptHashingService hashingService,
            BearerTokenService bearerTokenService,
            AuthMapper authMapper
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.farmerRepository = farmerRepository;
        this.advisorRepository = advisorRepository;
        this.hashingService = hashingService;
        this.bearerTokenService = bearerTokenService;
        this.authMapper = authMapper;
    }

    @Transactional
    public AuthenticationResult signUp(SignUpResource resource) {
        if (userRepository.existsByUsername(resource.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        UserRole role = UserRole.from(resource.role());
        User user = userRepository.save(User.builder()
                .username(resource.username())
                .password(hashingService.encode(resource.password()))
                .role(role)
                .build());

        Farmer farmer = null;
        Advisor advisor = null;

        if (role == UserRole.FARMER) {
            farmer = farmerRepository.save(Farmer.builder().user(user).build());
        } else if (role == UserRole.ADVISOR) {
            advisor = advisorRepository.save(Advisor.builder()
                    .user(user)
                    .rating(BigDecimal.ZERO)
                    .build());
        }

        Profile profile = profileRepository.save(Profile.builder()
                .user(user)
                .experience(0)
                .build());

        return buildAuthenticationResult(user, profile, farmer, advisor);
    }

    public AuthenticationResult signIn(SignInResource resource) {
        User user = userRepository.findByUsername(resource.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!hashingService.matches(resource.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        Profile profile = profileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "User is missing a profile"));

        Farmer farmer = user.getRole() == UserRole.FARMER
                ? farmerRepository.findByUser_Id(user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Farmer user is missing farmer data"))
                : null;
        Advisor advisor = user.getRole() == UserRole.ADVISOR
                ? advisorRepository.findByUser_Id(user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Advisor user is missing advisor data"))
                : null;

        return buildAuthenticationResult(user, profile, farmer, advisor);
    }

    private AuthenticationResult buildAuthenticationResult(
            User user,
            Profile profile,
            Farmer farmer,
            Advisor advisor
    ) {
        AuthenticatedUser authenticatedUser = authMapper.toAuthenticatedUser(user, profile, farmer, advisor);
        String token = bearerTokenService.generateToken(authenticatedUser);
        return new AuthenticationResult(authenticatedUser, token);
    }
}
