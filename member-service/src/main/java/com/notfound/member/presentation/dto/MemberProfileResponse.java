package com.notfound.member.presentation.dto;

import com.notfound.member.domain.model.Member;

import java.time.LocalDateTime;
import java.util.UUID;

public record MemberProfileResponse(
        UUID memberId,
        String email,
        String name,
        String phone,
        String role,
        boolean sellerRegistered,
        LocalDateTime createdAt
) {

    public static MemberProfileResponse from(Member member, boolean sellerRegistered) {
        return new MemberProfileResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhone(),
                member.getRole().name(),
                sellerRegistered,
                member.getCreatedAt()
        );
    }
}
