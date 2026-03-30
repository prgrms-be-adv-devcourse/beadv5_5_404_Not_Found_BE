package com.notfound.member.domain.event;

import java.util.UUID;

public record DepositChangedEvent(
        UUID memberId,
        int amount,
        Type type
) {
    public enum Type {
        CHARGE, DEDUCT
    }
}
