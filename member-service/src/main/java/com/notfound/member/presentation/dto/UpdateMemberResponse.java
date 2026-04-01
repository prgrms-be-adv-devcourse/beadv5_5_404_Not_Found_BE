package com.notfound.member.presentation.dto;

import com.notfound.member.domain.model.Member;

import java.util.UUID;

public record UpdateMemberResponse(
        UUID memberId,
        String name,
        String phone
) {
    public static UpdateMemberResponse from(Member member) {
        return new UpdateMemberResponse(member.getId(), member.getName(), member.getPhone());
    }
}
