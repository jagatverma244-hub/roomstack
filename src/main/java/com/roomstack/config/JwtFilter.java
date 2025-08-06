package com.roomstack.config;

import com.roomstack.entity.Login;
import com.roomstack.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String accessToken = null;
        String refreshToken = null;

        // 🔍 Extract cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                } else if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        // ✅ If valid access token → authenticate
        if (accessToken != null && jwtService.isValidAccessToken(accessToken)) {
            setAuthenticationFromToken(accessToken);
        }

        // ❌ Access expired → use refresh token if valid
        else if (refreshToken != null && jwtService.isValidRefreshToken(refreshToken)) {
            Optional<Login> optionalLogin = jwtService.getUserFromToken(refreshToken);
            if (optionalLogin.isPresent()) {
                Login login = optionalLogin.get();

                // 🛡️ Set authentication
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        login, null, List.of(new SimpleGrantedAuthority(login.getRole())));
                SecurityContextHolder.getContext().setAuthentication(auth);

                // 🔁 Generate new access token
                String newAccessToken = jwtService.generateAccessToken(login, 1); // 1 min
                Cookie newAccessCookie = new Cookie("accessToken", newAccessToken);
                newAccessCookie.setHttpOnly(true);
                newAccessCookie.setSecure(true);
                newAccessCookie.setPath("/");
                newAccessCookie.setMaxAge(60); // 1 minute
                response.addCookie(newAccessCookie);
            } else {
                // 🔁 Refresh token expired → session timeout
                SecurityContextHolder.clearContext();
                response.sendRedirect("/login?sessionExpired=true"); // ✅ fix here
                return;
            }
        }

        // ❌ Both tokens missing or invalid → session expired
        else {
            SecurityContextHolder.clearContext();
            response.sendRedirect("/login?sessionExpired=true"); // ✅ fix here
            return;
        }

        // ✅ Continue filter chain
        filterChain.doFilter(request, response);
    }
    // 🔐 Helper method to set authentication from token
    private void setAuthenticationFromToken(String token) {
        Optional<Login> optionalLogin = jwtService.getUserFromToken(token);
        if (optionalLogin.isPresent()) {
            Login login = optionalLogin.get();
            if (login.getRole() != null) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        login, null, List.of(new SimpleGrantedAuthority(login.getRole())));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
    }

    // 🚫 Don't apply this filter on login or public endpoints
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/login") ||
                path.equals("/logout") ||
                path.equals("/") ||

                path.startsWith("/pforgot") ||
                path.startsWith("/User")||

                path.startsWith("/css") ||
                path.startsWith("/js") ||
                path.startsWith("/images") ||
                path.startsWith("/api/public");
    }

}
