package com.roomstack.service;

import com.roomstack.dao.LoginsRepository;
import com.roomstack.entity.Login;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final LoginsRepository loginsRepository;
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public JwtService(LoginsRepository loginsRepository) {
        this.loginsRepository = loginsRepository;
    }

    public String generateAccessToken(Login user, int minutes) {
        return generateToken(user, minutes, ChronoUnit.MINUTES, "access");
    }

    public String generateRefreshToken(Login user, int days) {
        return generateToken(user, days, ChronoUnit.DAYS, "refresh");
    }


    private String generateToken(Login user, int amount, ChronoUnit unit, String type) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .claim("type", type)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(amount, unit)))
                .signWith(secretKey)
                .compact();
    }

    public boolean isValidAccessToken(String token) {
        return isValidToken(token, "access");
    }

    public boolean isValidRefreshToken(String token) {
        return isValidToken(token, "refresh");
    }

    private boolean isValidToken(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            return expectedType.equals(claims.get("type", String.class)) &&
                    claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    public Optional<Login> getUserFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            String username = claims.getSubject();
            return loginsRepository.findByUsernameIgnoreCase(username);
        } catch (Exception e) {
            System.out.println("Failed to extract user: " + e.getMessage());
            return Optional.empty();
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public Optional<TokenPair> refreshTokens(String refreshToken) {
        if (!isValidRefreshToken(refreshToken)) return Optional.empty();

        Optional<Login> userOpt = getUserFromToken(refreshToken);
        if (userOpt.isEmpty()) return Optional.empty();

        Login user = userOpt.get();

        String newAccess = generateAccessToken(user, 1);  // 1 minute
        String newRefresh = generateRefreshToken(user, 5); // 5 minutes

        return Optional.of(new TokenPair(newAccess, newRefresh));
    }

    public static class TokenPair {
        public final String accessToken;
        public final String refreshToken;

        public TokenPair(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    // ✅ NEW: Check valid access token from cookie
    public boolean hasValidAccessToken(HttpServletRequest request) {
        return getTokenFromCookie(request, "accessToken")
                .map(this::isValidAccessToken)
                .orElse(false);
    }

    // ✅ NEW: Check valid refresh token from cookie
    public boolean hasValidRefreshToken(HttpServletRequest request) {
        return getTokenFromCookie(request, "refreshToken")
                .map(this::isValidRefreshToken)
                .orElse(false);
    }

    // Helper: Extract token from cookie
    private Optional<String> getTokenFromCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}
