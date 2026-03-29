package com.notfound.member.presentation.dto;

import java.util.UUID;

public record DepositBalanceResponse(
        UUID memberId,
        int depositBalance
) {
}
