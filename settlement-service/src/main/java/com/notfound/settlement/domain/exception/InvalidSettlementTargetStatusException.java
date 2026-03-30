package com.notfound.settlement.domain.exception;

import com.notfound.settlement.domain.model.SettlementTargetStatus;

import java.util.UUID;

public class InvalidSettlementTargetStatusException extends RuntimeException {

    public InvalidSettlementTargetStatusException(UUID targetId, SettlementTargetStatus currentStatus) {
        super("정산 대상 상태 전이 불가. targetId=" + targetId + ", currentStatus=" + currentStatus);
    }
}
