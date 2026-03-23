package com.notfound.member.infrastructure.security;

import com.notfound.member.application.port.out.TokenBlacklistRepository;
import com.notfound.member.domain.model.MemberRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public JwtAuthenticationFilter(JwtProvider jwtProvider,
                                   TokenBlacklistRepository tokenBlacklistRepository) {
        this.jwtProvider = jwtProvider;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PublicPaths.isPublic(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            Claims claims = jwtProvider.parseOrNull(token);

            if (claims != null && !tokenBlacklistRepository.existsByJti(claims.getId())) {
                UUID memberId = UUID.fromString(claims.getSubject());
                MemberRole role = MemberRole.valueOf(claims.get("role", String.class));
                boolean emailVerified = claims.get("email_verified", Boolean.class);

                JwtAuthentication principal = new JwtAuthentication(memberId, role, emailVerified);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
