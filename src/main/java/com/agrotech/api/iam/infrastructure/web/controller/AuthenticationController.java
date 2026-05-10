package com.agrotech.api.iam.infrastructure.web.controller;

import com.agrotech.api.iam.infrastructure.web.dto.AuthenticatedUserResource;
import com.agrotech.api.iam.infrastructure.web.dto.SignInResource;
import com.agrotech.api.iam.infrastructure.web.dto.SignUpResource;
import com.agrotech.api.iam.application.mapper.AuthMapper;
import com.agrotech.api.iam.application.usecase.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/sign-up")
    public ResponseEntity<AuthenticatedUserResource> signUp(@RequestBody SignUpResource signUpResource) {
        return new ResponseEntity<>(authMapper.toAuthenticatedUserResource(authService.signUp(signUpResource)), HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource signInResource) {
        return ResponseEntity.ok(authMapper.toAuthenticatedUserResource(authService.signIn(signInResource)));
    }
}
