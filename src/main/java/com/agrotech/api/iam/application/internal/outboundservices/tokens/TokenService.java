package com.agrotech.api.iam.application.internal.outboundservices.tokens;

import java.util.List;

/**
 * TokenService interface
 * This interface is used to generate and validate tokens
 */
public interface TokenService {

    String generateToken(String username);

    String generateToken(String username, List<String> roles);

    String getUsernameFromToken(String token);

    boolean validateToken(String token);
}