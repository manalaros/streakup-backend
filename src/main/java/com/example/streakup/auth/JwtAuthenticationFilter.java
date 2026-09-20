package com.example.streakup.auth;

import com.example.streakup.user.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);

            try {
                if (!jwtService.isValid(token)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                String username = jwtService.extractUsername(token);

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    userRepository.findByUsernameIgnoreCase(username).ifPresent(user -> {
                        var authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user.getUsername(),
                                        null,
                                        java.util.List.of()
                                );

                        SecurityContextHolder
                                .getContext()
                                .setAuthentication(authentication);
                    });
                }
            } catch (JwtException | IllegalArgumentException ignored) {
                // Invalid tokens remain unauthenticated.
            }
        }

        filterChain.doFilter(request, response);
    }
}