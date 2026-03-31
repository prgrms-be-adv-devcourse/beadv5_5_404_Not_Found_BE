package com.notfound.member.infrastructure.init;

import com.notfound.member.application.port.out.MemberRepository;
import com.notfound.member.domain.model.Member;
import com.notfound.member.domain.model.MemberRole;
import com.notfound.member.domain.model.MemberStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminAccountInitializer(MemberRepository memberRepository,
                                   PasswordEncoder passwordEncoder,
                                   @Value("${admin.email:admin@bookcommerce.com}") String adminEmail,
                                   @Value("${admin.password:}") String adminPassword) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_PASSWORD 미설정 — ADMIN 계정 시딩 스킵");
            return;
        }

        if (memberRepository.existsByEmail(adminEmail)) {
            log.info("ADMIN 계정 이미 존재: {}", adminEmail);
            return;
        }

        Member admin = Member.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .name("관리자")
                .phone("00000000000")
                .role(MemberRole.ADMIN)
                .status(MemberStatus.ACTIVE)
                .pointBalance(0)
                .depositBalance(0)
                .emailVerified(true)
                .build();

        memberRepository.save(admin);
        log.info("ADMIN 계정 생성 완료: {}", adminEmail);
    }
}
