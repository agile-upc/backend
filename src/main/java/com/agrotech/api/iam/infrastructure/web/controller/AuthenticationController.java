package com.agrotech.api.iam.infrastructure.web.controller;

import com.agrotech.api.iam.infrastructure.web.dto.AuthenticatedUserResource;
import com.agrotech.api.iam.infrastructure.web.dto.RefreshTokenResource;
import com.agrotech.api.iam.infrastructure.web.dto.SignInResource;
import com.agrotech.api.iam.infrastructure.web.dto.SignUpResource;
import com.agrotech.api.iam.application.mapper.AuthMapper;
import com.agrotech.api.iam.application.usecase.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthenticationController {
    private final AuthService authService;
    private final AuthMapper authMapper;

    public AuthenticationController(AuthService authService, AuthMapper authMapper) {
        this.authService = authService;
        this.authMapper = authMapper;
    }

    @Operation(summary = "Sign up", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = SignUpResource.class))))
    @PostMapping(value = "/sign-up", consumes = "multipart/form-data")
    public ResponseEntity<AuthenticatedUserResource> signUp(@Valid @ModelAttribute SignUpResource signUpResource) throws IOException {
        return new ResponseEntity<>(authMapper.toAuthenticatedUserResource(authService.signUp(signUpResource)), HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticatedUserResource> signIn(@Valid @RequestBody SignInResource signInResource) {
        return ResponseEntity.ok(authMapper.toAuthenticatedUserResource(authService.signIn(signInResource)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticatedUserResource> refresh(@Valid @RequestBody RefreshTokenResource refreshTokenResource) {
        return ResponseEntity.ok(authMapper.toAuthenticatedUserResource(authService.refreshSession(refreshTokenResource.refreshToken())));
    }
}
