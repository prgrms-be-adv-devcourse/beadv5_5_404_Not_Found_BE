package com.notfound.member.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Member Service Security 설정
 *
 * JWT 검증은 Gateway에서 전담한다.
 * member-service는 Gateway가 전달한 X-User-Id, X-Role, X-Email-Verified 헤더를 신뢰하며,
 * 자체적으로 JWT 필터를 등록하지 않는다.
 *
 * /internal/** 경로는 InternalSecretFilter가 X-Internal-Secret 헤더를 검증하여 보호한다.
 * 헤더가 없거나 불일치하면 필터에서 즉시 403을 반환하고 요청이 컨트롤러에 도달하지 않는다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final InternalSecretFilter internalSecretFilter;

    public SecurityConfig(InternalSecretFilter internalSecretFilter) {
        this.internalSecretFilter = internalSecretFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(internalSecretFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
