package com.farma_ya.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private long jwtRefreshExpiration;

    @Value("${jwt.issuer:farmaya-api}")
    private String jwtIssuer;

    @Value("${jwt.audience:farmaya-client}")
    private String jwtAudience;

    private SecretKey getSigningKey() {
        // Decode Base64 encoded secret key for enhanced security
        byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("authorities", authorities)
                .claim("type", "access")
                .issuer(jwtIssuer)
                .audience().add(jwtAudience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();

        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtRefreshExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuer(jwtIssuer)
                .audience().add(jwtAudience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer(jwtIssuer)
                    .requireAudience(jwtAudience)
                    .build();

            Claims claims = parser.parseSignedClaims(token).getPayload();

            // Additional validation for token type
            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                logger.error("Token type invalid: expected 'access', found '{}'", tokenType);
                return false;
            }

            return true;
        } catch (ExpiredJwtException ex) {
            logger.error("Token JWT expirado: {}", ex.getMessage());
            return false;
        } catch (UnsupportedJwtException ex) {
            logger.error("Token JWT no soportado: {}", ex.getMessage());
            return false;
        } catch (MalformedJwtException ex) {
            logger.error("Token JWT malformado: {}", ex.getMessage());
            return false;
        } catch (SecurityException ex) {
            logger.error("Firma JWT inválida: {}", ex.getMessage());
            return false;
        } catch (IllegalArgumentException ex) {
            logger.error("Token JWT vacío o nulo: {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            logger.error("Error validando token JWT: {}", ex.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer(jwtIssuer)
                    .requireAudience(jwtAudience)
                    .build();

            Claims claims = parser.parseSignedClaims(token).getPayload();

            // Validate that this is a refresh token
            String tokenType = claims.get("type", String.class);
            return "refresh".equals(tokenType);
        } catch (JwtException | IllegalArgumentException ex) {
            logger.error("Refresh token JWT inválido: {}", ex.getMessage());
            return false;
        }
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    public long getRefreshExpirationTime() {
        return jwtRefreshExpiration;
    }
}