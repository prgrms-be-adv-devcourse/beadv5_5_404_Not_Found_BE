package com.notfound.payment.application.service;

import com.notfound.payment.application.port.in.GetDepositHistoryUseCase;
import com.notfound.payment.application.port.out.DepositPort;
import com.notfound.payment.domain.model.Deposit;
import com.notfound.payment.domain.model.DepositType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositHistoryService implements GetDepositHistoryUseCase {

    private final DepositPort depositPort;

    @Override
    @Transactional(readOnly = true)
    public HistoryResult getHistory(UUID memberId, DepositType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Deposit> result = (type == null)
                ? depositPort.findByMemberId(memberId, pageable)
                : depositPort.findByMemberIdAndType(memberId, type, pageable);

        return new HistoryResult(
                result.getContent().stream().map(this::toEntry).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private DepositEntry toEntry(Deposit d) {
        return new DepositEntry(
                d.getId(),
                d.getType(),
                d.getAmount(),
                d.getBalanceAfter(),
                d.getDescription(),
                d.getOrderId(),
                d.getCreatedAt()
        );
    }
}
