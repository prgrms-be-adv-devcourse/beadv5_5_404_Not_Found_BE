package com.notfound.settlement.domain.exception;

import java.util.UUID;

public class SettlementTargetNotFoundException extends RuntimeException {

    public SettlementTargetNotFoundException(UUID id) {
        super("정산 대상을 찾을 수 없습니다. id=" + id);
    }
}
