package com.notfound.settlement.adapter.in.rest.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.YearMonth;

public record SettlementExecuteRequest(
        @NotNull(message = "정산 대상 월은 필수입니다.")
        @PastOrPresent(message = "미래 월은 정산할 수 없습니다.")
        YearMonth targetMonth
) {}
