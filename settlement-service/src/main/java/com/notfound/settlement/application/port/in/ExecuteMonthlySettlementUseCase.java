package com.notfound.settlement.application.port.in;

import java.time.YearMonth;

public interface ExecuteMonthlySettlementUseCase {

    void execute(YearMonth targetMonth);
}
