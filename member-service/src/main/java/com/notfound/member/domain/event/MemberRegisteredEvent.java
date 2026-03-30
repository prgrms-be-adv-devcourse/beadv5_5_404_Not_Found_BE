package com.notfound.member.domain.event;

import java.util.UUID;

public record MemberRegisteredEvent(
        UUID memberId,
        String email,
        String name
) {
}
