package com.notfound.member.application.port.in;

import com.notfound.member.domain.model.Member;

import java.util.UUID;

public interface GetMemberProfileUseCase {
    Member getProfile(UUID memberId);
}
