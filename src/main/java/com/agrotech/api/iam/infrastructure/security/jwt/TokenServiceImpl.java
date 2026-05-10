package com.agrotech.api.iam.infrastructure.security.jwt;

import com.agrotech.api.iam.domain.model.AuthenticatedUser;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class TokenServiceImpl implements BearerTokenService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);
    private static final String AUTHORIZATION_PARAMETER_NAME = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final int TOKEN_BEGIN_INDEX = 7;

    @Value("${authorization.jwt.secret}")
    private String secret;

    @Value("${authorization.jwt.expiration.days}")
    private int expirationDays;

    @Override
    public String generateToken(AuthenticatedUser authenticatedUser) {
        authenticatedUser.validate();

        var issuedAt = new Date();
        var expiration = DateUtils.addDays(issuedAt, expirationDays);

        return Jwts.builder()
                .subject(authenticatedUser.username())
                .claim("userId", authenticatedUser.userId())
                .claim("profileId", authenticatedUser.profileId())
                .claim("role", authenticatedUser.role().name())
                .claim("farmerId", authenticatedUser.farmerId())
                .claim("advisorId", authenticatedUser.advisorId())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public AuthenticatedUser getAuthenticatedUser(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                claims.get("userId", Long.class),
                claims.get("profileId", Long.class),
                claims.getSubject(),
                UserRole.from(claims.get("role", String.class)),
                claims.get("farmerId", Long.class),
                claims.get("advisorId", Long.class)
        );
        authenticatedUser.validate();
        return authenticatedUser;
    }

    @Override
    public boolean validateToken(String token) {
        try {
            getAuthenticatedUser(token);
            return true;
        } catch (SignatureException exception) {
            LOGGER.error("Invalid JSON Web Token signature: {}", exception.getMessage());
        } catch (MalformedJwtException exception) {
            LOGGER.error("Invalid JSON Web Token: {}", exception.getMessage());
        } catch (ExpiredJwtException exception) {
            LOGGER.error("JSON Web Token is expired: {}", exception.getMessage());
        } catch (UnsupportedJwtException exception) {
            LOGGER.error("JSON Web Token is unsupported: {}", exception.getMessage());
        } catch (IllegalArgumentException exception) {
            LOGGER.error("JSON Web Token is invalid: {}", exception.getMessage());
        }
        return false;
    }

    @Override
    public String getBearerTokenFrom(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_PARAMETER_NAME);
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_BEGIN_INDEX);
        }
        return null;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
