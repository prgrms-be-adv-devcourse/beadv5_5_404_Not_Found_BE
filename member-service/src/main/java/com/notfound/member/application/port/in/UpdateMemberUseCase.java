package com.notfound.member.application.port.in;

import com.notfound.member.application.port.in.command.UpdateMemberCommand;
import com.notfound.member.domain.model.Member;

import java.util.UUID;

public interface UpdateMemberUseCase {

    Member updateMember(UUID memberId, UpdateMemberCommand command);
}
