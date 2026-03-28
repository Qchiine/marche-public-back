package com.emsi.marches_backend.security;

import com.emsi.marches_backend.model.UtilisateurDocument;
import com.emsi.marches_backend.model.enums.RoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final String jwtSecret;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    private SecretKey signingKey;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs
    ) {
        this.jwtSecret = jwtSecret;
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @PostConstruct
    void init() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UtilisateurDocument utilisateur) {
        return buildToken(utilisateur, TOKEN_TYPE_ACCESS, accessExpirationMs);
    }

    public String generateRefreshToken(UtilisateurDocument utilisateur) {
        return buildToken(utilisateur, TOKEN_TYPE_REFRESH, refreshExpirationMs);
    }

    public boolean isAccessTokenValid(String token) {
        return isTokenValid(token, TOKEN_TYPE_ACCESS);
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token, TOKEN_TYPE_REFRESH);
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public RoleEnum extractRole(String token) {
        String role = parseClaims(token).get("role", String.class);
        return RoleEnum.valueOf(role);
    }

    private String buildToken(UtilisateurDocument utilisateur, String type, long expirationMs) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(utilisateur.getEmail())
                .claim("uid", utilisateur.getId())
                .claim("role", utilisateur.getRole().name())
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }

    private boolean isTokenValid(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            String actualType = claims.get("type", String.class);
            return expectedType.equals(actualType) && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
