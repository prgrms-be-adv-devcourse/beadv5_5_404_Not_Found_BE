package com.notfound.settlement.adapter.in.rest;

import com.notfound.settlement.adapter.in.rest.request.SettlementExecuteRequest;
import com.notfound.settlement.application.port.in.ExecuteMonthlySettlementUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/settlements")
public class SettlementAdminController {

    private final ExecuteMonthlySettlementUseCase executeMonthlySettlementUseCase;

    @PostMapping("/execute")
    public ResponseEntity<Void> executeSettlement(@RequestBody SettlementExecuteRequest request) {
        executeMonthlySettlementUseCase.execute(request.targetMonth());
        return ResponseEntity.ok().build();
    }
}
