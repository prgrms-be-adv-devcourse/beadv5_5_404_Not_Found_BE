package com.notfound.member.application.port.in;

import java.util.UUID;

public interface CheckMemberActiveUseCase {

    boolean isActiveMember(UUID memberId);
}
