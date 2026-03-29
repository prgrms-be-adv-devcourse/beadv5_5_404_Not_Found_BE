package com.notfound.settlement.domain.exception;

import com.notfound.settlement.domain.model.SettlementStatus;

import java.util.UUID;

public class InvalidSettlementStatusException extends RuntimeException {

    public InvalidSettlementStatusException(UUID settlementId, SettlementStatus currentStatus) {
        super("정산 상태 전이 불가. settlementId=" + settlementId + ", currentStatus=" + currentStatus);
    }
}
